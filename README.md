# storage-atom

storage-atom is a
[ClojureScript](https://github.com/clojure/clojurescript) library that
provides an easy way to create atoms backed by
[HTML5 Web Storage](http://en.wikipedia.org/wiki/Web_storage).

Any change in the atom will be saved into the web storage.

The reverse is also true. This means that an atom modified in a tab
 or a window will also be modified in all of them.

[![Build Status](https://travis-ci.org/alandipert/storage-atom.png?branch=master)](https://travis-ci.org/alandipert/storage-atom)

## Usage

### Dependency

```clojure
[alandipert/storage-atom "1.2.2"]
```

### Example

```clojure
;; Require or use alandipert.storage-atom in your namespace.
;; The primary functions it provides are html-storage and local-storage.
;; It also provides the IStorageBackend protocol.

(ns your-ns
  (:require [alandipert.storage-atom :refer [local-storage]]))

;; Persist atom to HTML localStorage. The local-storage function takes an
;; atom and a key to store with, and returns the atom. If the key in storage
;; isn't set it will be initialized with the value obtained by dereferencing
;; the provided atom. Otherwise the atom's value will be reset! with the value
;; obtained from localStorage. All subsequent swap! and reset! operations on
;; the atom will cause the value in localStorage to be updated.

(def prefs (local-storage (atom {}) :prefs))

;; You can use the atom normally now - values are transparently persisted.

(add-watch prefs
           :new
           (fn [_ _ _ v]
             (.log js/console "new preference" v)))

(swap! prefs assoc :bg-color "red")

(:bg-color @prefs) ;=> "red"

;; Check that current value has been stored in localStorage.

(.getItem js/localStorage ":prefs") ;=> "{:bg-color \"red\"}"
```

## Notes

Because web storage keys and values are stored as strings, only values
that can be printed readably may be used as storage keys or values.

I haven't done any performance testing, but this approach is much
slower than using web storage directly because the entire atom contents
are written on every `swap!`.

To prevent superfluous writes to the local storage, there is a 10 ms
debounce. It can be modified with the `storage-delay` atom or the
`*storage-delay*` dynamic var. :

```clj
(reset! alandipert.storage-atom/storage-delay 100) ;; permanently
                                                   ;; increase
                                                   ;; debounce to 100
                                                   ;; ms

(binding [alandipert.storage-atom/*storage-delay* 500]
	 ... do some stuff ... ) ;; temporarily increase debounce to
                                 ;; 500 ms

```


[enduro](https://github.com/alandipert/enduro) is a Clojure library
that provides similar functionality by using files or a database for
storage.

The cross-window propagation doesn't always work if browsing the
`.html` directly instead of passing throught a webserver.
(Yes Chrome, I'm looking at you...)

## Testing

[PhantomJS](http://phantomjs.org/) 1.7.0 is used for unit testing.
With it installed, you can run the tests like so:

    lein cljsbuild test

## License

Copyright © 2013 Alan Dipert

Distributed under the Eclipse Public License, the same as Clojure.
