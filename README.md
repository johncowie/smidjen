# smidjen

## What is it?

It's just a smidjen of [midje](https://github.com/marick/Midje).

For those of you who love the arrow (```=>```) syntax of Midje, but don't need a lot of the funky stuff that comes with it. Also for those of you who want a pared-down clojurescript version of Midje.

## Usage

You can test using the ```fact``` blocks and the arrow form. The facts compile down to clojure.test code.

e.g.

```clojure
(require '[smidjen.core :refer [facts fact]])

(facts "about equating stuff"
  (fact "about Party dogma"
    (+ 2 2) => 5)
  (fact "about three"
    3 => "is the magic number"))

;; equivalent to:
;; (deftest about-equating-stuff
;;     (testing "about Party dogma"
;;       (is (= 5 (+ 2 2))))
;;     (testing "about three"
;;       (is (= "is the magic number" 3))))
```

### Test selectors

When you run your tests with `lein test-refresh` you can filter them using (e.g.) `lein test-refresh :fast`.

Providing you have this configuration in `project.clj`:
```clojure
:test-selectors {:default     (constantly nil)
                 :integration :integration
                 :unit        (complement :integration)
                 :fast        (complement :slow)}
```
all tests that have been marked with `:slow` won't be run
```clojure
(facts :slow "I am very slow"
	(prn "You are not gonna see this message when you run lein test-refresh :fast"))
```

### ClojureScript

You can also use smidjen for clojurescript testing (it compiles down to cljs.test in this case). Just ensure that you use ```:refer-macros``` in the requires.

e.g.

```clojure
(require '[smidjen.core :refer-macros [facts fact]])

(facts "cljs fact"
  (fact :a => :b))

;; equivalent to:
;; (cljs.test/deftest cljs-fact
;;   (cljs.test/is (= :b :a)))
```

### Not arrow

You can use the ```=not=>``` for the negative case.

e.g.

```clojure
(fact 2 =not=> 3) ;; test passes
```

### Predicate checkers

You can use single-argument predicate functions on the right-hand-side of the arrow form to test properties of a value.

e.g.

```clojure
(fact "Is 2 even?" 2 => even?) ;; test passes
```

### Autotest

Midje offers autotest functionality for watching for code changes and then automatically re-running the tests (i.e. ```lein midje :autotest```).  Since smidjen compiles to clojure.test or cljs.test, test-watching libraries can be used that support these.  E.g. *lein-test-refresh* for clj and *doo* for cljs.

### Who tests the tests?

```lein test```
