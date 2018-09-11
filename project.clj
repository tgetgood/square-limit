(defproject square-limit "0.1.0-SNAPSHOT"
  :description "Implementation of Henderson's Functional Geometry paper (2002)"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]]

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]]

  :source-paths ["src" "dev" "../falloleen/src"]

  :main square-limit.core

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src" "../falloleen/src"]

     :figwheel     {:on-jsload "square-limit.core/on-reload"}

     :compiler     {:main                 square-limit.core
                    :asset-path           "js/compiled/out"
                    :output-to            "resources/public/js/compiled/square-limit.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :parallel-build       true
                    :checked-arrays       :warn
                    :source-map-timestamp true
                    :preloads             [devtools.preload]}}

    {:id           "min"
     :source-paths ["src" "../falloleen/src"]
     :compiler     {:output-to      "resources/public/js/compiled/square-limit.js"
                    :asset-path     "js/compiled/out"
                    :main           square-limit.core
                    :parallel-build true
                    :optimizations  :advanced
                    :pretty-print   false}}]}

  :profiles
  {:dev  {:dependencies  [[net.cgrand/macrovich "0.2.1"]
                          [quil "2.7.1" :exclusions [[org.clojure/clojure]]]

                          [binaryage/devtools "0.9.9"]
                          [cider/piggieback "0.3.1"]
                          [figwheel-sidecar "0.5.16"]
                          [org.clojure/test.check "0.9.0"]
                          [org.clojure/tools.namespace "0.2.11"]]
          ;; need to add dev source path here to get user.clj loaded
          :source-paths  ["src" "dev" "../falloleen/src"]

          :repl-options  {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
          ;; need to add the compliled assets to the :clean-targets
          :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                            :target-path]}})
