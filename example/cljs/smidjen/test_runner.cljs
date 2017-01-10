(ns smidjen.test-runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [smidjen.example-test]))

(enable-console-print!)

(doo-tests 'smidjen.example-test)
