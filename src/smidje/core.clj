(ns smidje.core
  (:require [clojure.string :as str]
            [clojure.walk :refer [postwalk]]))

(defn cljs-env? [env]
  (boolean (:ns env)))

(defn- qualify [prefix s]
  (symbol (str (name prefix) "/" (name s))))

(def syms {::deftest :test
           ::testing :test
           ::is      :test
           ::println :core
           ::=       :core
           ::not     :core
           ::fn?     :core})

(defn qualify-sym [ns-map sym-key]
  (if-let [ns-key (get syms sym-key)]
    (qualify (ns-key ns-map) sym-key)
    sym-key))

(defn qualify-syms [ns-map body]
  (postwalk (partial qualify-sym ns-map) body))

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
       (not (arrow-form? expected))))                       ;; TODO if middle is arrow and others aren't, throw exception

(defn primitive? [v]
  (and (not (seq? v))
       (not (fn? v))
       (not (symbol? v))))

(defn wrap-is [pred]
  `(::is ~pred))

(defn wrap-is-not [pred]
  `(::is (::not ~pred)))

(defn- assert-eq [wrapper actual expected]
  (if (primitive? expected)
    (wrapper `(::= ~expected ~actual))
    `(if (::fn? ~expected)
       ~(wrapper `(~expected ~actual))
       ~(wrapper `(::= ~expected ~actual)))))

(defn- make-assertion [actual s expected]
  (when (valid-assertion? actual s expected)
    (cond (eq-arrow? s)
          (assert-eq wrap-is actual expected)
          (not-eq-arrow? s)
          (assert-eq wrap-is-not actual expected))))

(defn- assertions [body]
  (scan (partial make-assertion) 3 body))

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

(defn- testing-expr [body]
  (if (string? (first body))
    `(::testing ~(first body) ~@(rest body))
    `(::testing "NO_DESCRIPTION" ~@body)))

(defn- deftest-expr [body]
  (if (string? (first body))
    `(::deftest ~(deftest-sym (first body)) ~(expand-in-nested-context (rest body)))
    `(::deftest ~(gensym) ~(expand-in-nested-context body))))

(defn- wrap-testing-block [env body]
  (if (has-nested-sym env)
    (testing-expr body)
    (deftest-expr body)))

(def cljs-ns {:test 'cljs.test :core 'cljs.core})
(def clj-ns {:test 'clojure.test :core 'clojure.core})

(defn get-env-ns [env]
  (if (cljs-env? env)
    cljs-ns
    clj-ns))

(defn expand-future-fact [env [d & _]]
  (let [ns-map (get-env-ns env)]
    (->>
      (if (has-nested-sym env)
        `(::println ~(str "\nWORK TO DO: " d))
        `(::deftest ~(deftest-sym d) (::println ~(str "\nWORK TO DO: " d))))
      (qualify-syms ns-map))))

(defn expand-fact [env body]
  (let [ns-map (get-env-ns env)]
    (if (sequential? body)
      (->> body assertions (wrap-testing-block env) (qualify-syms ns-map))
      body)))

(defmacro fact [& body]
  (expand-fact &env body))

(defmacro facts [& body]
  (expand-fact &env body))

(defmacro future-fact [& body]
  (expand-future-fact &env body))

(defmacro future-facts [& body]
  (expand-future-fact &env body))