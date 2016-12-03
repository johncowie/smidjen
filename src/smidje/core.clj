(ns smidje.core)

(declare => =not=> fact facts future-fact future-facts)

(defn- qualify [prefix s]
  (symbol (str (name prefix) "/" (name s))))

(defn- try-resolve [s]
  (if (symbol? s)
    (resolve s)
    s))

(defn- smidje-sym? [f smidje-ns s]
  (= (str "#'" smidje-ns "/" f)
     (str (try-resolve s))))

(def ^:private eq-arrow? (partial smidje-sym? '=>))
(def ^:private not-eq-arrow? (partial smidje-sym? '=not=>))
(def ^:private fact? (partial smidje-sym? 'fact))
(def ^:private facts? (partial smidje-sym? 'facts))
(def ^:private future-fact? (partial smidje-sym? 'future-fact))
(def ^:private future-facts? (partial smidje-sym? 'future-facts))

(defn- arrow-form? [smidje-ns s]
  (or (eq-arrow? smidje-ns s)
      (not-eq-arrow? smidje-ns s)))

(defn- fact-form? [smidje-ns s]
  (or (fact? smidje-ns s)
      (facts? smidje-ns s)))

(defn- future-fact-form? [smidje-ns s]
  (or (future-fact? smidje-ns s)
      (future-facts? smidje-ns s)))

(defn- scan
  [f n s]
  (let [ss (take n s)]
    (if (= (count ss) n)
      (if-let [r (apply f ss)]
        (cons r (scan f n (drop n s)))
        (cons (first s) (scan f n (rest s))))
      s)))

(defn- valid-assertion? [smidje-ns actual s expected]
  (and (arrow-form? smidje-ns s)
       (not (arrow-form? smidje-ns actual))
       (not (arrow-form? smidje-ns expected))))                                                         ;; TODO if middle is arrow and others aren't, throw exception

(defn- make-assertion [test-ns smidje-ns actual s expected]
  (when (valid-assertion? smidje-ns actual s expected)
    (cond (eq-arrow? smidje-ns s) `(~(qualify test-ns 'is) (= ~expected ~actual))
          (not-eq-arrow? smidje-ns s) `(~(qualify test-ns 'is) (not (= ~expected ~actual))))))

(defn- assertions [test-ns smidje-ns body]
  (scan (partial make-assertion test-ns smidje-ns) 3 body))

(defn- wrap-testing-block [test-ns body]
  (if (string? (first body))
    `(~(qualify test-ns 'testing) ~(first body) ~@(rest body))
    `(~(qualify test-ns 'testing) ~@body)))


(defn future-fact-expr [[d & _]]
  `(prn ~(str "WORK TO DO: " d)))

(defn- expand-nested-facts [test-ns smidje-ns body]
  (if (sequential? body)
    (let [[f & r] body]
      (cond
        (fact-form? smidje-ns f)
        (->> r (map (partial expand-nested-facts test-ns smidje-ns)) (assertions test-ns smidje-ns) (wrap-testing-block test-ns))
        (future-fact-form? smidje-ns f)
        (future-fact-expr r)
        :else
        (->> body (map (partial expand-nested-facts test-ns smidje-ns)) (assertions test-ns smidje-ns))))
    body))

(defn expand-facts [test-ns smidje-ns body]
  (if (sequential? body)
    (->> body (map (partial expand-nested-facts test-ns smidje-ns)) (assertions test-ns smidje-ns) (wrap-testing-block test-ns))
    body))

(defmacro fact [& body]
  (expand-facts 'clojure.test 'smidje.core body))

(defmacro facts [& body]
  (expand-facts 'clojure.test 'smidje.core body))

(defmacro future-fact [& body]
  (future-fact-expr body))

(defmacro future-facts [& body]
  (future-fact-expr body))
