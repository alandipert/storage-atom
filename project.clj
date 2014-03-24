(defproject alandipert/storage-atom "1.2.3"
  :description "ClojureScript atoms backed by HTML5 web storage."
  :url "https://github.com/alandipert/storage-atom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/cljs"]
  :dependencies [[org.clojure/clojurescript "0.0-2138"]
	         [tailrecursion/cljson "1.0.6"]]
  :plugins [[lein-cljsbuild "1.0.1"]]
  :cljsbuild {:builds {:test
                       {:source-paths ["test"]
                        :compiler {:output-to "public/test.js"
                                   :optimizations :advanced}
                        :jar false}}
              :test-commands {"unit" ["phantomjs" "test/runner.js"]}})
