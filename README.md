# storage-atom

storage-atom is a
[ClojureScript](https://github.com/clojure/clojurescript) library that
provides a reference type similar to the
[native atom](http://clojure.org/atoms), except that storage-atoms are
persisted with [HTML5 Web Storage](http://en.wikipedia.org/wiki/Web_storage).

[![Build Status](https://travis-ci.org/alandipert/storage-atom.png?branch=master)](https://travis-ci.org/alandipert/storage-atom)

## Usage

### Dependency

```clojure
[alandipert/storage-atom "1.0.0"]
```

### Example

```clojure
;; Require or use alandipert.storage-atom in your
;; namespace. The primary functions it provides are storage-atom, swap!, reset!.

(ns your-ns
  (:require [alandipert.storage-atom :as sa])

;; Call sa/storage-atom with a value, a store, and a key within the store
;; to use.  If the key in storage isn't set, it will be initialized with
;; value.  If the key already points to a value, your initial value will
;; be ignored and the existing data will be read.

(def prefs (sa/storage-atom {} js/localStorage "prefs")

;; You can add watches to storage atoms like regular atoms.

(add-watch prefs
           :new
           (fn [_ _ _ v]
             (.log js/console "new preference" v)))

;; sa/swap! is similar to Clojure's swap!.

(sa/swap! prefs assoc :bg-color "red")

(:bg-color @prefs) ;=> "red"
(.getItem js/localStorage "\"prefs\"") ;=> "{:bg-color \"red\"}"
```

## Notes

Because web storage keys and values are stored as strings, only values
that can be printed readably may be used as storage keys or values.

I haven't done any performance testing, but this approach is much
slower than using web storage directly because the entire atom contents
are written on every `swap!`.

[enduro](https://github.com/alandipert/enduro) is a Clojure library
that provides similar functionality by using files or a database for
storage.

## Testing

[PhantomJS](http://phantomjs.org/) is used for unit testing.  With it
installed, you can run the tests with:

    lein cljsbuild test

## License

Copyright © 2013 Alan Dipert

Distributed under the Eclipse Public License, the same as Clojure.
