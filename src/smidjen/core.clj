(ns smidjen.core
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

;; Need to write version of scan that doesn't modify collection (i.e. vector to lazy-seq)
(defn- scan
  [f n c]
  (let [subc (take n c)]
    (if (= (count subc) n)
      (if-let [r (apply f subc)]
        (cons r (scan f n (drop n c)))
        (cons (first c) (scan f n (rest c))))
      c)))

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
  (cond (primitive? expected) (wrapper `(::= ~expected ~actual))
        :else `(if (::fn? ~expected)
                 ~(wrapper `(~expected ~actual))
                 ~(wrapper `(::= ~expected ~actual)))))

(defn throws-exp? [expected]
  (and (list? expected)
       ((smidje-sym? 'throws) (first expected))))

(defn thrown-exp [actual [_ exception-type]]
  `(::is (~'thrown? ~exception-type ~actual)))

(defn- make-assertion [actual s expected]
  (when (valid-assertion? actual s expected)
    (case [(eq-arrow? s) (throws-exp? expected)]
      [true false] (assert-eq wrap-is actual expected)
      [false false] (assert-eq wrap-is-not actual expected)
      [true true] (thrown-exp actual expected)
      (throw (Exception. "SOMETHING HAS GONE MAJORLY WRONG")))))

(defn- assertions [body]
  (scan (partial make-assertion) 3 body))

(defn- rewrite-arrows [body]
  (postwalk
    (fn [v] (if (and (sequential? v) (not (vector? v))) (assertions v) v))
    body))

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

(defn- with-keyword [kw s]
  (if kw (with-meta s {kw true}) s))

(defn- deftest-expr [[kw body]]
  (if (string? (first body))
    `(::deftest ~(with-keyword kw (deftest-sym (first body))) ~(expand-in-nested-context (rest body)))
    `(::deftest ~(with-keyword kw (gensym)) ~(expand-in-nested-context body))))

(defn- extract-keyword [body]
  (if (keyword? (first body))
    [(first body) (rest body)]
    [nil body]))

(defn- wrap-testing-block [env body]
  (if (has-nested-sym env)
    (-> body testing-expr)                                  ;; arrows already rewritten by deftest-expr
    (-> body rewrite-arrows extract-keyword deftest-expr)))

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
    (->> body (wrap-testing-block env) (qualify-syms ns-map))))

(defmacro fact [& body]
  (expand-fact &env body))

(defmacro facts [& body]
  (expand-fact &env body))

(defmacro future-fact [& body]
  (expand-future-fact &env body))

(defmacro future-facts [& body]
  (expand-future-fact &env body))
