(ns smidjen.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidjen.core :as sm :refer [expand-fact expand-future-fact fact facts future-fact future-facts]]
            [cljs.analyzer :refer [resolve-var]]))

(defn stub-gen-sym
  ([vs]
   (let [a (atom vs)]
     (fn g
       ([] (g "G__"))
       ([prefix] (let [f (first @a)] (swap! a rest) (symbol (str prefix f)))))))
  ([] (stub-gen-sym (range))))

(defmacro predictable-macroexpand-1 [form]
  `(with-redefs [clojure.core/gensym (stub-gen-sym)
                 smidjen.core/nested-sym (symbol "nested-sym")]
     (print-str (macroexpand-1 ~form))))

(defmacro fact-with-env [env & body]
  (expand-fact env body))

(defmacro future-with-env [env & body]
  (expand-future-fact env body))

(def test-cases
  {
   "basic fact without string description"
   ['(fact 2 => 2)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 2 2))))]

   "facts synonym"
   ['(facts 3 => 2)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 2 3))))]

   "facts with keyword"
   ['(facts :slow 3 => 2)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 2 3))))]

   "fact with string description"
   ['(fact "3 is 3" 3 => 3)
    `(deftest ~(symbol "_3-is-3") (let [~'nested-sym 1] (is (= 3 3))))]

   "fact with a string description and two assertions"
   ['(fact "test with two facts"
           (+ 2 2) => 5
           (+ 1 3) => 4)
    `(deftest ~(symbol "test-with-two-facts")
       (let [~'nested-sym 1]
         (is (clojure.core/= 5 ~'(+ 2 2)))
         (is (clojure.core/= 4 ~'(+ 1 3)))))]

   "nested fact (i.e. wrapped with nested-sym let binding) - arrows not rewritten"
   ['(fact-with-env {nested-sym 1}
                    "some maths"
                    (+ 4 5) => 9
                    (- 10 8) => 2)
    `(testing "some maths"
       ~'(+ 4 5) ~'=> ~'9
       ~'(- 10 8) ~'=> ~'2)]

   "nested fact with no description - arrows not rewritten"
   ['(fact-with-env {nested-sym 1}
                    (+ 2 2) => 5)
    `(testing "NO_DESCRIPTION" ~'(+ 2 2) ~'=> ~'5)]

   "fact with assertions separated by another statement"
   ['(fact "a" => "a"
           (prn "something")
           "b" => "b")
    `(deftest ~'G__0
       (let [~'nested-sym 1]
         (is (= "a" "a"))
         ~'(prn "something")
         (is (= "b" "b"))))]

   "the =not=checker"
   ['(fact (+ 1 1) =not=> 3)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (not (= 3 ~'(+ 1 1))))))]

   "ns alias doesn't break things"
   ['(sm/fact (+ 1 1) => 3)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 3 ~'(+ 1 1)))))]

   "future-fact prints out to console"
   ['(future-fact "tbd" (+ 1 1) => 3)
    `(deftest ~'tbd (println "\nWORK TO DO: tbd"))]

   "future-facts does the same thing"
   ['(future-facts "tbd" (+ 1 1) => 3)
    `(deftest ~'tbd (println "\nWORK TO DO: tbd"))]

   "if future-fact is nested, then is not wrapped in deftest"
   ['(future-with-env {nested-sym 1} "something")
    `(println "\nWORK TO DO: something")]

   "syms that aren't written by macro aren't qualified"
   ['(fact "f" (= 2 2) => true)
    `(deftest ~'f (let [~'nested-sym 1] (is (= true ~'(= 2 2)))))]

   "can pass a predicate to the right hand side"
   ['(fact "f" 3 => even?)
    `(deftest ~'f (let [~'nested-sym 1] (if (fn? ~'even?)
                                          (is (~'even? 3))
                                          (is (= ~'even? 3)))))]

   "can pass a symbol to the right hand side"
   ['(fact "f" 3 => answer)
    `(deftest ~'f (let [~'nested-sym 1]
                    (if (fn? ~'answer)
                      (is (~'answer 3))
                      (is (= ~'answer 3)))))]

   "if :ns is present in environment, then assumes clojurescript"
   ['(fact-with-env {:ns "cljs"}
                    3 => 1)
    `(cljs.test/deftest ~'G__0 (let [~'nested-sym 1] (cljs.test/is (cljs.core/= 1 3))))]

   "can nest fact inside of let binding and arrow will still get rewritten"
   ['(fact "f" (let [x 1 y 2] x => y))
    `(deftest ~'f (let [~'nested-sym 1]
                    (~'let [~'x 1 ~'y 2]
                      (if (fn? ~'y)
                        (is (~'y ~'x))
                        (is (= ~'y ~'x))))))]

   "can assert on thrown exception"
   ['(fact "f" (+ 1 2) => (throws AssertionError))
    `(deftest ~'f
       (let [~'nested-sym 1]
         (is (~'thrown? ~'AssertionError ~'(+ 1 2)))
         ))]
   })


;; TODO test nested future-facts
;; TODO nesting works at compile time - but what about functions that wrap facts, then being called inside another fact block?

(def expanded
  (->> test-cases
       (map (fn [[test-name [actual expected]]]
              [test-name
               [(predictable-macroexpand-1 actual)
                (print-str expected)]]))
       (into {})))

(deftest fact-macro
  (doseq [[test-name [actual expected]] expanded]
    (testing test-name
      (is (= expected actual)))))

(def slow-test (macroexpand-1 '(fact :slow 1 => 12)))
(def fast-test (macroexpand-1 '(fact 1 => 12)))

(deftest slow-vs-fast
  (letfn [(slow? [form] (-> form second meta :slow))]
    (is (true? (slow? slow-test)))
    (is (not (true? (slow? fast-test))))))
