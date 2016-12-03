(ns smidje.core
  (:require [clojure.test :refer [is testing deftest]]))

(declare => =not=> fact facts future-fact)

(def qual=> #'smidje.core/=>)
(def qual=not=> #'smidje.core/=not=>)
(def qual=>fact #'smidje.core/fact)
(def qual=>facts #'smidje.core/facts)

(defn scan
  ([f n s] (scan f (constantly true) n s))
  ([f p n s]
   (let [ss (take n s)]
     (if (= (count ss) n)
       (if (apply p ss)
         (cons (apply f ss) (scan f p n (drop n s)))
         (cons (first s) (scan f p n (rest s))))
       s))))

(defn try-resolve [s]
  (if (symbol? s)
    (if-let [q (resolve s)]
      q
      (throw (Exception. (str "Couldn't find " s))))
    s))

(defn arrow-form? [s]
  (#{qual=> qual=not=>} (try-resolve s)))

(defn eq-arrow? [s]
  (= qual=> (try-resolve s)))

(defn not-eq-arrow? [s]
  (= qual=not=> (try-resolve s)))

(defn valid-assertion? [actual s expected]
  (and (arrow-form? s)
       (not (arrow-form? actual))
       (not (arrow-form? expected))))                                                         ;; TODO if middle is arrow and others aren't, throw exception

(defn make-assertion [actual s expected]
  (cond (eq-arrow? s) `(is (= ~expected ~actual))
        (not-eq-arrow? s) `(is (not (= ~expected ~actual)))))

(defn assertions [body]
  (scan make-assertion valid-assertion? 3 body))

(defn wrap-testing-block [body]
  (if (string? (first body))
    `(testing ~(first body) ~@(rest body))
    `(testing ~@body)))

(defn fact-form? [s]
  (#{qual=>fact qual=>facts} (try-resolve s)))

(defn future-fact-form? [s]
  (#{#'smidje.core/future-fact} (try-resolve s)))

(defn future-fact-expr [[d & _]]
  `(prn ~(str "WORK TO DO: " d)))

(defn expand-nested-facts [body]
  (if (sequential? body)
    (let [[f & r] body]
      (cond
        (fact-form? f)
        (->> r (map expand-nested-facts) assertions wrap-testing-block)
        (future-fact-form? f)
        (future-fact-expr r)
        :else
        (->> body (map expand-nested-facts) assertions)))
    body))

(defn expand-facts [body]
  (if (sequential? body)
    (->> body (map expand-nested-facts) assertions wrap-testing-block)
    body))

(defmacro fact [& body]
  (expand-facts body))

(defmacro facts [& body]
  (expand-facts body))

(defmacro future-fact [& body]
  (future-fact-expr body))
