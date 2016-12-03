(ns smidje.core)

(declare => =not=> fact facts future-fact future-facts)

(def qual=> #'smidje.core/=>)
(def qual=not=> #'smidje.core/=not=>)
(def qual=>fact #'smidje.core/fact)
(def qual=>facts #'smidje.core/facts)
(def qual=>future-fact #'smidje.core/future-fact)
(def qual=>future-facts #'smidje.core/future-facts)

(defn qualify [prefix s]
  (symbol (str (name prefix) "/" (name s))))

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

(defn make-assertion [test-ns actual s expected]
  (cond (eq-arrow? s) `(~(qualify test-ns 'is) (= ~expected ~actual))
        (not-eq-arrow? s) `(~(qualify test-ns 'is) (not (= ~expected ~actual)))))

(defn assertions [test-ns body]
  (scan (partial make-assertion test-ns) valid-assertion? 3 body))

(defn wrap-testing-block [test-ns body]
  (if (string? (first body))
    `(~(qualify test-ns 'testing) ~(first body) ~@(rest body))
    `(~(qualify test-ns 'testing) ~@body)))

(defn fact-form? [s]
  (#{qual=>fact qual=>facts} (try-resolve s)))

(defn future-fact-form? [s]
  (#{qual=>future-fact qual=>future-facts} (try-resolve s)))

(defn future-fact-expr [[d & _]]
  `(prn ~(str "WORK TO DO: " d)))

(defn expand-nested-facts [test-ns body]
  (if (sequential? body)
    (let [[f & r] body]
      (cond
        (fact-form? f)
        (->> r (map (partial expand-nested-facts test-ns)) (assertions test-ns) (wrap-testing-block test-ns))
        (future-fact-form? f)
        (future-fact-expr r)
        :else
        (->> body (map (partial expand-nested-facts test-ns)) (assertions test-ns))))
    body))

(defn expand-facts [test-ns body]
  (if (sequential? body)
    (->> body (map (partial expand-nested-facts test-ns)) (assertions test-ns) (wrap-testing-block test-ns))
    body))

(defmacro fact [& body]
  (expand-facts 'clojure.test body))

(defmacro facts [& body]
  (expand-facts 'clojure.test body))

(defmacro future-fact [& body]
  (future-fact-expr body))

(defmacro future-facts [& body]
  (future-fact-expr body))
