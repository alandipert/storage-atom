(ns alandipert.storage-atom.test.macros)

(defmacro with-local-storage-testing-scope
  [& body]
  `(binding [alandipert.storage-atom/*storage-delay* :none]
     ~@body
     (.clear js/localStorage)))
