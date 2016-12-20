- Can't test recursive macros the same way in clojure as in clojurescript, because macroexpand in clojurescript IS A MACRO
- This means that the only options are: testing non-recursive macros
  - Can test by modifiying the environment (i.e. passing in the goodness that tells lower macro calls that something is nested or not
  - Can maybe test by capturing output of tests - although for reasons previously explored this is also kinda a pain in the arse
- Other complications in cljs-land
   - stubbing out gen-sym (could be added to environment?)
   - Stubbing out namespace map (again could be added to environment?)

QUESTIONS:
 - can &env be modified in tests? NO
 -

FINAL APPROACH:
 - don't test the macros
 - just write clojure test for functions underneath the macros
