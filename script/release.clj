#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[babashka.tasks :refer [shell]]
         '[clojure.string :as str])

(fs/copy "resources/public/index.html" "gh-pages"
         {:replace-existing true})
(shell "clojure -M:dev -m shadow.cljs.devtools.cli release main")
(def index-file (fs/file "gh-pages" "index.html"))
(spit index-file (str/replace (slurp index-file) "main.js" "sci_script_tag.js"))
(fs/create-dirs (fs/file "gh-pages" "js"))
(fs/copy (fs/file "resources" "public" "js" "main.js")
         (fs/file "gh-pages" "js" "sci_script_tag.js")
         {:replace-existing true})

(def with-gh-pages (partial shell {:dir "gh-pages"}))
(with-gh-pages "git add .")
(with-gh-pages "git commit -m 'update build'")
(with-gh-pages "git push origin gh-pages")

nil
