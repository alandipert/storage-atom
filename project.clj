(defproject alandipert/storage-atom "1.0.0"
  :description "ClojureScript atoms backed by HTML5 web storage."
  :url "https://github.com/alandipert/storage-atom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/cljs"]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :cljsbuild {:builds {:test
                       {:source-paths ["test"]
                        :compiler {:output-to "public/test.js"
                                   :optimizations :advanced}
                        :jar false}}
              :test-commands {"unit" ["phantomjs" "test/runner.js"]}})
