(ns smidje.example
  (:require
    [clojure.test :refer :all]
    [smidje.core :refer [fact facts =>]]))

;;(comment
;;  (deftest first-fact
;;    (fact "a fact"
;;          (+ 2 2) => 6
;;          (fact (+ 2 2) => 5
;;                (+ 2 4) => 5))))
;
;
;;; The following results in only the first a-test being executed
;;(deftest my-test
;;  (deftest a-test (is (= 2 3))))
;;
;;(deftest my-other-test
;;  (deftest a-test (is (= 3 4))))
;
;;; The following results in both a-tests being executed, and then the
;;;  first one that's def'd is executing again by the test runner
;;(deftest my-test
;;  ((deftest a-test (is (= 2 3))))
;;  )
;;(deftest my-other-test
;;  ((deftest a-test (is (= 3 4))))
;;  )
;
;;; The following form allows tests with the same name to both be executed
;;;  - also means that order of execution can be controlled
;;;  - However: two test-ns-hooks can't be defined
;;(defn test-ns-hook []
;;  ((deftest b-test (is (= 1 2))
;;                   ((deftest a-test (is (= 3 4))))))
;;  ((deftest b-test (is (= 7 8))
;;                   ((deftest a-test (is (= 9 10)))))))
;
;(deftest a
;  (testing "a" (is (= 1 2)))
;  (testing "b" (is (= 2 3)))
;  (testing "c" (is (= 3 4)))
;  (testing "d" (is (= 4 5)))
;  )
;
;;(test-ns-hook)
;
;#_(with-test (declare gilbert)
;             (is (= 1 2))
;             (with-test (declare bledyn)
;                        (is (= 3 4)))
;             (with-test (declare bledyn)
;                        (is (= 4 5))))
;
;;; TODO questions to answer
;;;  can ordering be controlled?
;;;    - NOPE
;;;  is there some alternative to deftest that allows nesting of tests properly
;;;    - Not really, go back to walking the whole thing so only the top level fact is a deftest
;;;  does deftest maintain order of tested tests?
;;;    - NOPE - only with all that test-ns-hook crap
;;;  does deftest maintain order of testing blocks
;;;    - YES
;
;
;;; CAN ONLY NEST TWO TESTS TOGETHER!!!
;
;
;;; THINGS TO RULE OUT FOR NOW:
;;;  SECURING ORDER OF TOP LEVEL FACT BLOCKS
;;;  NESTING deftest's in a meaningful way
;
;(def x (atom 0))
;
;(defmacro a []
;  (swap! x inc)
;  (let [v @x]
;    `(prn ~v)))
;
;(def def-registry (atom {}))
;
;;; Proof of concept for hacking defs into unique names
;
;(defn get-sym [s]
;  (let [sym (symbol s)]
;    (if-let [c (get @def-registry sym)]
;      (do (swap! def-registry update sym inc)
;          (symbol (str s "-" c)))
;      (do
;        (swap! def-registry assoc sym 1)
;        (symbol s)))))
;
;(defmacro defo [name & body]
;  `(def ~(get-sym name) ~@body))
;
;(defo "bilbo" 1)
;(defo "bilbo" 3)
;(defo "bilbo" 7)
;
;
;(def c (atom 0))
;
;(defmacro defnc [name & body]
;  (swap! c inc)
;  (let [v @c]
;    `(defn ~name []
;       ~@body
;       ~v)))
;
;(defnc fa
;       (defnc faa
;              (defnc faaa)
;              (defnc faab))
;       (defnc fab
;              (defnc faba)
;              (defnc fabb)))
;
;;; => evaluated in the order shown::
;
;;; so, can this idea be used to identify the top-level context??
;;;  not this way -- maybe with dynamic binding (a la *earmuffs*)?
;(def layer (atom -1))
;
;(defmacro defl [name & body]
;  (swap! layer inc)
;  (let [v @layer
;        b `(defn ~name []
;             ~@body
;             ~v)]
;    (swap! layer dec)
;    b))
;
;(defl rootf
;      (defl child1a
;            (defl child2a))
;      (defl child1b
;            (defl child2b)))
;
;(def ^:dynamic *a* 0)
;
;(defmacro del [name & body]
;  (binding [*a* (inc *a*)]
;    `(defn ~name []
;       ~@body
;       ~*a*)))
;
;(defmacro show-env [] (println &env))
;
;(defmacro show-form [& body]
;  (println &form)
;  `(do ~@body)
;  )
;
;(show-form
;  (show-form)
;  )
;
;(defn exactf [body]
;  (if (sequential? body)
;    (if (#{#'+, #'-, #'*, #'/} (first body))
;      (cons (first body) (map exactf (rest body)))
;      body)
;    (if (number? body)
;      (rationalize body)
;      body)))
;
;(exactf '(+ 1 2.5))
;
;(defmacro exact [& body]
;  (exactf body))
;
;(exact (+ 1 2))
;
;
;
;(defmacro m1 [& body]
;  (prn &env)
;  `(let [a# 1]
;     ~@body))
;
;(defmacro f1 [& body]
;  (prn &form)
;  `(do ~@body))
;
;(def a (atom 0))
;
;(def ^:dynamic *p* 0)
;
;(def n (gensym))
;
;(defn is-nested? [env-map]
;  (contains? env-map n))
;
;(defmacro n1 [& body]
;  (prn (is-nested? &env))
;  `(let [~n 1]
;     ~@body))
;;; NOTE this approach works
;
;;; e.g.
;
;(defn f [& items]
;  (prn "TOP LEVEL: " items))
;
;(defn g [& items]
;  (prn "NESTED: " items))
;
;(defmacro w [& body]
;  (prn &env)
;  (if (is-nested? &env)
;    `(g (let [~n 1] ~@body))
;    `(f (let [~n 1] ~@body))))
;
;(w 1 2
;   (w 4 5)
;   (w 5 6
;      (w 7)))
;
;(def ^:dynamic *nn* false)
;
;(defmacro t [& body]
;  (if *nn*
;    (println "NESTED: " body)
;    (println "TOP" body))
;  (binding [*nn* true]
;    (clojure.walk/macroexpand-all body)))
;
;(t + 2 (t - 3 1))

;; NESTED APPROACHES
;;  One: Search for symbol in nested structure -
;;    if found, plant something in there so that it know's that it is tested
;;    advantage of this is that nested macro calls will still expand
;;    how to resolve symbol though if just a macro call??

;;  Two: Wrap each lower call in a let binding, so that the env can be checked
;;    for a gen-sym -> this one works...

;; Three: Try to change expansion order by trying to eval lower stuff...


;; FIXME checking for &env is not going to work in
;; clojurescript if fact is wrapped in a let binding

;; this works -
;; so could maintain some kind of perverse counter for tagging different tests


;; GENERAL APPROACH IS THIS
;;  go back to walking the tree




