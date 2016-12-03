# smidje

### _*NB* this is WIP and nowhere near clojars yet_

### What is it?

It's just a smidje of [midje](https://github.com/marick/Midje).

For those of you who love the arrow (```=>```) syntax of Midje, but don't need a lot of the funky stuff that comes with it.

### Usage

You can test equality using the ```fact``` blocks and the arrow form. The facts compile down to clojure.test code.  At present you still need to wrap your facts in the ```deftest``` form.

e.g.

```clojure
(require ['clojure.test :refer [deftest]]
         ['smidje.core :refer [facts fact =>]])

(deftest my-test
  (facts "about equating stuff"
    (fact "about Party dogma"
      (+ 2 2) => 5)
    (fact "about three"
      3 => "is the magic number")))

;; equivalent to:
;; (deftest my-test
;;   (testing "about equating stuff"
;;     (testing "about Party dogma"
;;       (is (= 5 (+ 2 2))))
;;     (testing "about three"
;;       (is (= "is the magic number" 3)))))
```
