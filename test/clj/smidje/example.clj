(ns smidje.example
  (:require
    [clojure.test :refer [deftest is testing]]
    [smidje.core :refer [fact facts =>]]))

(comment
  (deftest first-fact
    (fact "a fact"
          (+ 2 2) => 6
          (fact (+ 2 2) => 5
                (+ 2 4) => 5))))

;`(+ 1 ~`(2 3))
;
;(type `(2 3))
;
;(type '(2 3))
;
;`~`+
;
;;`~fact
;
;(defn b [body]
;  `(bob ~(first body) ~@(rest body)))
;
;(defmacro m [& body]
;  (str (b body)))
;
;(m (+ 1 2) (+ 3 2))
;
;(macroexpand-1 '(m (+ 1 1) (+ 2 2) (+ 3 3)))
;
;(prn `fact)



