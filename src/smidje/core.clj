(ns smidje.core
  (:require [clojure.test :refer [is testing deftest]]))

(defn scan
  ([f n s] (scan f (constantly true) n s))
  ([f p n s]
   (let [ss (take n s)]
     (if (= (count ss) n)
       (if (apply p ss)
         (cons (apply f ss) (scan f p n (drop n s)))
         (cons (first s) (scan f p n (rest s))))
       s))))

(defn arrow-form? [s]
  (#{'=> '=not=>} s))

(defn valid-assertion? [actual s expected]
  (and (arrow-form? s)
       (not (arrow-form? actual))
       (not (arrow-form? expected))))                       ;; TODO if middle is arrow and others aren't, throw exception

(defn make-assertion [actual s expected]
  (cond (= s '=>) `(is (= ~expected ~actual))
        (= s '=not=>) `(is (not (= ~expected ~actual)))))

(defn assertions [body]
  (scan make-assertion valid-assertion? 3 body))

(defn wrap-testing-block [body]
  (if (string? (first body))
    `(testing ~(first body) ~@(rest body))
    `(testing ~@body)))

(defn expand-nested-facts [body]
  (if (sequential? body)
    (if (#{'fact 'facts} (first body))
      (->> (rest body) (map expand-nested-facts) assertions wrap-testing-block)
      (->> body (map expand-nested-facts) assertions))
    body))

(defn expand-facts [body]
  (if (sequential? body)
    (->> body (map expand-nested-facts) assertions wrap-testing-block)
    body))

(defmacro fact [& body]
  (expand-facts body))

(defmacro facts [& body]
  (expand-facts body))

(def => :arrow)
(def =not=> :not-arrow)


