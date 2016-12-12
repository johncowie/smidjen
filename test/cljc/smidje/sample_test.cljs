(ns smidje.sample-test
  (:require
    [cljs.test :refer-macros [testing deftest is]]
    [smidje.core :refer [fact facts future-fact future-facts => =not=>] :include-macros true]
    ))

;; TODO What happens for nested deftest's with the same name?

;((deftest a-test
;          (testing "glarg glarg" (is (= 1 2)))
;          ((deftest another-test
;                    (testing "blarb" (is (= 1 2)))))))
;
;(deftest b-test
;         (future-fact "A test")
;         (fact "Another test"
;               1 => 2
;               3 => 4
;               (future-fact "Blah")))
;
;(b-test)


