(ns smidje.core
  (:require [clojure.string :as str]))

(defn cljs-env? [env]
  (boolean (:ns env)))

(defn- qualify [prefix s]
  (symbol (str (name prefix) "/" (name s))))

(defn- smidje-sym? [f]
  (fn [s]
    (and (symbol? s) (= (name s) (str f)))))

(def ^:private eq-arrow? (smidje-sym? '=>))
(def ^:private not-eq-arrow? (smidje-sym? '=not=>))

(defn- arrow-form? [s]
  (or (eq-arrow? s)
      (not-eq-arrow? s)))

(defn- scan
  [f n s]
  (let [ss (take n s)]
    (if (= (count ss) n)
      (if-let [r (apply f ss)]
        (cons r (scan f n (drop n s)))
        (cons (first s) (scan f n (rest s))))
      s)))

(defn- valid-assertion? [actual s expected]
  (and (arrow-form? s)
       (not (arrow-form? actual))
       (not (arrow-form? expected))))                   ;; TODO if middle is arrow and others aren't, throw exception

(defn- make-assertion [ns-map actual s expected]
  (when (valid-assertion? actual s expected)
    (cond (eq-arrow? s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) '=) ~expected ~actual))
          (not-eq-arrow? s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) 'not) (~(qualify (:core ns-map) '=) ~expected ~actual))))))

(defn- assertions [ns-map body]
  (scan (partial make-assertion ns-map) 3 body))

(def nested-sym (gensym))

(defn has-nested-sym [env]
  (if (cljs-env? env)
    (contains? (:locals env) nested-sym)
    (contains? env nested-sym)))

(defn expand-in-nested-context [body]
  `(let [~nested-sym 1]
     ~@body))

(defn- deftest-sym [s]
  (-> s
      (str/replace #"[^a-zA-Z0-9]+" "-")
      (str/replace #"^[0-9]" #(str "_" %1))
      symbol))

(defn- testing-expr [ns-map body]
  (if (string? (first body))
    `(~(qualify (:test ns-map) 'testing) ~(first body) ~@(rest body))
    `(~(qualify (:test ns-map) 'testing) "NO_DESCRIPTION" ~@body)))

(defn- deftest-expr [ns-map body]
  (if (string? (first body))
    `(~(qualify (:test ns-map) 'deftest) ~(deftest-sym (first body)) ~(expand-in-nested-context (rest body)))
    `(~(qualify (:test ns-map) 'deftest) ~(gensym) ~(expand-in-nested-context body))))

(defn- wrap-testing-block [ns-map env body]
  (if (has-nested-sym env)
    (testing-expr ns-map body)
    (deftest-expr ns-map body)))


(def cljs-ns {:test 'cljs.test :core 'cljs.core})
(def clj-ns {:test 'clojure.test :core 'clojure.core})

(defn get-env-ns [env]
  (if (cljs-env? env)
    cljs-ns
    clj-ns))

(defn expand-future-fact [env [d & _]]
  (let [ns-map (get-env-ns env)]
    `(~(qualify (:core ns-map) 'println) ~(str "WORK TO DO: " d))))

(defn expand-fact [env body]
  (let [ns-map (get-env-ns env)]
    (if (sequential? body)
      (->> body (assertions ns-map) (wrap-testing-block ns-map env))
      body)))

(defmacro fact [& body]
  (expand-fact &env body))

(defmacro facts [& body]
  (expand-fact &env body))

(defmacro future-fact [& body]
  (expand-future-fact &env body))

(defmacro future-facts [& body]
  (expand-future-fact &env body))