(ns smidje.cljs-test
  (:require
    [smidje.cljs :refer [fact facts => =not=> future-fact future-facts]]
    [clojure.test :as t]
    [cljs.test :refer [is testing]]))

(def expansions
  {
   "fact"
   ['(fact 1 => 2)
    `(testing (is (= 2 1)))]

   "facts"
   ['(facts "a fact" (fact 1 =not=> 2))
    `(testing "a fact" (testing (is (not (= 2 1)))))]

   "future-fact"
   ['(future-fact "my fact" 1 => 2)
    `(prn "WORK TO DO: my fact")]

   "future-facts"
   ['(fact (future-facts "bob")
           (facts 1 => 2))
    `(testing (prn "WORK TO DO: bob")
              (testing (is (= 2 1))))]
   })

(def test-cases
  (doall
    (for [[description [actual expected]] expansions]
      [description
       (str expected)
       (str (macroexpand-1 actual))])))

(t/deftest fact-macro
  (doseq [[desc expected actual] test-cases]
    (t/testing desc
      (t/is (= expected actual)))))
