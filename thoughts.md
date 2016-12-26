PREDICATES
 - if RHS is a data primitive, then leave it alone
 - if RHS is a symbol or an expression (i.e. (fn []), then wrap in if statement and decide what to do at run time

```
  (if (fn? rhs)
    (is (rhs lhs))
    (is (= rhs lhs)))
```

TODOS
 - [X] Remove whitespace from top level test names
 - [X] Make separate directory/profile for example tests
 - [X] Deal with case where fact string begins with a number
 - [ ] Still wrap future-facts in deftest block
 - [ ]
