(ns smidje.core
  (:require [cljs.analyzer :refer [resolve-var]]))

(declare => =not=> fact facts future-fact future-facts)

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
(def ^:private fact? (smidje-sym? 'fact))
(def ^:private facts? (smidje-sym? 'facts ))
(def ^:private future-fact? (smidje-sym? 'future-fact ))
(def ^:private future-facts? (smidje-sym? 'future-facts ))

(defn- arrow-form? [env s]
  (or (eq-arrow? env s)
      (not-eq-arrow? env s)))

(defn- fact-form? [env s]
  (or (fact? env s)
      (facts? env s)))

(defn- future-fact-form? [env s]
  (or (future-fact? env s)
      (future-facts? env s)))

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
       (not (arrow-form? env expected))))                                                         ;; TODO if middle is arrow and others aren't, throw exception

(defn- make-assertion [env ns-map actual s expected]
  (when (valid-assertion? env actual s expected)
    (cond (eq-arrow? env s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) '=) ~expected ~actual))
          (not-eq-arrow? env s)
          `(~(qualify (:test ns-map) 'is) (~(qualify (:core ns-map) 'not) (~(qualify (:core ns-map) '=) ~expected ~actual))))))

(defn- assertions [env ns-map body]
  (doall (scan (partial make-assertion env ns-map) 3 body)))

(defn- wrap-testing-block [ns-map body]
  (if (string? (first body))
    `(~(qualify (:test ns-map) 'testing) ~(first body) ~@(rest body))
    `(~(qualify (:test ns-map) 'testing) ~@body)))

(defn future-fact-expr [ns-map [d & _]]
  `(~(qualify (:core ns-map) 'prn) ~(str "WORK TO DO: " d)))

(defn expand-nested-facts [env ns-map body]
  (if (sequential? body)
    (let [[f & r] body]
      (cond
        (fact-form? env f)
        (doall (->> r (map (partial expand-nested-facts env ns-map)) (assertions env ns-map) (wrap-testing-block ns-map)))
        (future-fact-form? env f)
        (future-fact-expr ns-map r)
        :else
        (doall (->> body (map (partial expand-nested-facts env ns-map)) (assertions env ns-map)))))
    body))

(defn expand-facts [env ns-map body]
  (if (sequential? body)
    (doall (->> body (map (partial expand-nested-facts env ns-map)) (assertions env ns-map) (wrap-testing-block ns-map)))
    body))

(def cljs-ns {:test 'cljs.test :core 'cljs.core})
(def clj-ns {:test 'clojure.test :core 'clojure.core})

(defmacro fact [& body]
  (if &env
    (expand-facts &env cljs-ns body)
    (expand-facts &env clj-ns body)))

(defmacro facts [& body]
  (if &env
    (expand-facts &env cljs-ns body)
    (expand-facts &env clj-ns body)))

(defmacro future-fact [& body]
  (if &env
    (future-fact-expr cljs-ns body)
    (future-fact-expr clj-ns body)))

(defmacro future-facts [& body]
  (if &env
    (future-fact-expr cljs-ns body)
    (future-fact-expr clj-ns body)))
