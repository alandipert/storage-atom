(set-env!
  :dependencies '[[org.clojure/clojure         "1.7.0"           :scope "provided"]
                  [org.clojure/clojurescript   "1.7.122"         :scope "test"]
                  [adzerk/boot-cljs            "1.7.48-3"        :scope "test"]
                  [adzerk/bootlaces            "0.1.10"          :scope "test"]
                  [crisptrutski/boot-cljs-test "0.2.0-SNAPSHOT" :scope "test"]
                  [com.cognitect/transit-cljs  "0.8.243"]]
  :source-paths #{"test"}
  :resource-paths #{"src"})

(require '[adzerk.bootlaces :refer :all]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(def +version+ "2.0.1")

(bootlaces! +version+)

(task-options!
 test-cljs {:js-env :phantom
            :exit? true
            :namespaces '#{alandipert.storage-atom.test}}
 pom  {:project     'alandipert/storage-atom
       :version     +version+
       :description "ClojureScript atoms backed by HTML5 web storage."
       :url         "https://github.com/alandipert/storage-atom"
       :scm         {:url "https://github.com/alandipert/storage-atom"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
