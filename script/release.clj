#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[babashka.tasks :refer [shell]])

(fs/copy "resources/public/index.html" "gh-pages"
         {:replace-existing true})

(fs/copy "resources/public/base.html" "gh-pages"
         {:replace-existing true})

(fs/copy "resources/public/tictactoe.html" "gh-pages"
         {:replace-existing true})

(fs/copy "resources/public/bookmarklet.html" "gh-pages"
         {:replace-existing true})

(fs/copy "resources/public/disable_auto_eval.html" "gh-pages"
         {:replace-existing true})

(def html-source-dir (fs/file "resources" "public" "html"))
(def html-target-dir (fs/file "gh-pages" "html"))
(fs/create-dirs html-target-dir)
(doseq [html ["export.html" "reagent.html"]]
  (fs/copy (fs/file html-source-dir html) html-target-dir))

(def style-source-dir (fs/file "resources" "public" "css"))
(def style-target-dir (fs/file "gh-pages" "css"))
(fs/create-dirs style-target-dir)
(fs/copy "resources/public/css/style.css" style-target-dir
         {:replace-existing true})

(def js-source-dir (fs/file "resources" "public" "js"))
(def js-target-dir (fs/file "gh-pages" "js"))
(fs/create-dirs js-target-dir)

(println "Compiling CLJS")
(shell "bb prod")

(fs/copy "resources/public/js/report.html" "gh-pages"
         {:replace-existing true})

(def index-file (fs/file "gh-pages" "index.html"))

(def cljs-source-dir (fs/file "resources" "public" "cljs"))
(def cljs-target-dir (fs/file "gh-pages" "cljs"))
(fs/create-dirs cljs-target-dir)

(run! (fn [f]
        (println "Copying" (str f))
        (fs/copy f
                 cljs-target-dir
                 {:replace-existing true}))
      (fs/glob cljs-source-dir "*.cljs"))

(run! (fn [f]
        (println "Copying" (str f))
        (fs/copy f
                 js-target-dir
                 {:replace-existing true}))
      (fs/glob js-source-dir "scittle*.js"))

(def with-gh-pages (partial shell {:dir "gh-pages"}))
(with-gh-pages "git add .")
(with-gh-pages "git commit -m 'update build'")
(with-gh-pages "git push origin gh-pages")

nil
