(ns smidje.exp
  (:require [clojure.test :refer [testing]]
            [clojure.walk :refer [macroexpand-all]]
            [smidje.core :as sm]))

(defmacro w [& body]
  `[~@body])

(type '(w 1 2))

(= (str (macroexpand-all '(sm/fact 1 (sm/fact 3 4))))
   (str (macroexpand-all '(testing 1 (testing 3 4)))))

(macroexpand-all (w 1 2 (w 3 4 5)))