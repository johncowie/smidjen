(ns smidje.core
  (:require [cljs.analyzer :refer [resolve-var]]))

(declare => =not=>)

(defn- qualify [prefix s]
  (symbol (str (name prefix) "/" (name s))))

(defn resolve-from-env [env s]
  (when env
    (str "#'" (:name (resolve-var env s)))))

(defn- try-resolve [env s]
  (if (symbol? s)
    (or (resolve-from-env env s) (str (resolve s)))
    s))

(defn- smidje-sym? [f]
  (fn [env s]
    (= (str "#'" 'smidje.core "/" f)
       (try-resolve env s))))

(def ^:private eq-arrow? (smidje-sym? '=>))
(def ^:private not-eq-arrow? (smidje-sym? '=not=>))

(defn- arrow-form? [env s]
  (or (eq-arrow? env s)
      (not-eq-arrow? env s)))

(defn- scan
  [f n s]
  (let [ss (take n s)]
    (if (= (count ss) n)
      (if-let [r (apply f ss)]
        (cons r (scan f n (drop n s)))
        (cons (first s) (scan f n (rest s))))
      s)))

(defn- valid-assertion? [env actual s expected]
  (and (arrow-form? env s)
       (not (arrow-form? env actual))
       (not (arrow-form? env expected))))                   ;; TODO if middle is arrow and others aren't, throw exception

(defn- make-assertion [env ns-map actual s expected]
  (when (valid-assertion? env actual s expected)
    (cond (eq-arrow? env s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) '=) ~expected ~actual))
          (not-eq-arrow? env s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) 'not) (~(qualify (:core ns-map) '=) ~expected ~actual))))))

(defn- assertions [env ns-map body]
  (doall (scan (partial make-assertion env ns-map) 3 body)))

(def ^:dynamic *nested?* false)

(defn expand-in-nested-context [form]
  (binding [*nested?* true]
    (clojure.walk/macroexpand-all form)))

(defn- wrap-testing-block [ns-map env body]
  (if *nested?*
    (if (string? (first body))
      `(~(qualify (:test ns-map) 'testing) ~(first body) ~@(rest body))
      `(~(qualify (:test ns-map) 'testing) "NO_DESCRIPTION" ~@body))
    (expand-in-nested-context
      (if (string? (first body))
        `(~(qualify (:test ns-map) 'deftest) ~(symbol (first body)) ~@(rest body))
        `(~(qualify (:test ns-map) 'deftest) ~(gensym) ~@body)))))

(defn future-fact-expr [ns-map [d & _]]
  `(~(qualify (:core ns-map) 'println) ~(str "WORK TO DO: " d)))

(defn expand-facts [env ns-map body]
  (if (sequential? body)
    (->> body (assertions env ns-map) (wrap-testing-block ns-map env))
    body))

(def cljs-ns {:test 'cljs.test :core 'cljs.core})
(def clj-ns {:test 'clojure.test :core 'clojure.core})

(defn get-env-ns [env]
  (if env cljs-ns clj-ns))

(defmacro fact [& body]
  (expand-facts &env (get-env-ns &env) body))

(defmacro facts [& body]
  (expand-facts &env (get-env-ns &env) body))

(defmacro future-fact [& body]
  (future-fact-expr (get-env-ns &env) body))

(defmacro future-facts [& body]
  (if &env
    (future-fact-expr (get-env-ns &env) body)
    (future-fact-expr (get-env-ns &env) body)))
