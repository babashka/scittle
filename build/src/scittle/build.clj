(ns scittle.build
  "Provides bb tasks for building and releasing scittle"
  (:require
   [babashka.classpath :as classpath]
   [babashka.fs :as fs]
   [babashka.tasks :refer [clojure]]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn- feature-files
  []
  (filter fs/exists?
          (map (fn [d]
                 (fs/file d "scittle_plugin.edn"))
               (classpath/split-classpath (classpath/get-classpath)))))

(defn- read-configs
  [files]
  (->> files
       (mapcat (comp edn/read-string slurp str))))

(defn- build-cmd [cmd scittle-dir]
  (let [files (feature-files)
        feature-configs (read-configs files)
        ;; Each ./src/scittle_plugin.edn has a ./deps.edn
        feature-dirs (map (comp fs/parent fs/parent) files)
        cmd' (if (seq files)
               (format "-Sdeps '%s' %s"
                       {:deps
                        (merge (into {}
                                     (map (fn [dir]
                                            [(symbol (str (fs/file-name dir) "/deps"))
                                             {:local/root (str dir)}])
                                          feature-dirs))
                               {'scittle/deps {:local/root scittle-dir}})}
                       cmd)
               cmd)]
    (when (seq feature-configs)
      (println "Building features:" (str/join ", " (map :name feature-configs)) "..."))
    (if (seq feature-configs)
      (apply str cmd'
        (map (fn [m] (format " --config-merge '%s'" (pr-str (:shadow-config m))))
             feature-configs))
      cmd')))

(defn- build*
  [cmd]
  (let [building-outside-scittle? (not (fs/exists? "shadow-cljs.edn"))
        scittle-dir (when building-outside-scittle?
                  (->> (classpath/get-classpath)
                       classpath/split-classpath
                       ;; Pull out scittle from local/root or git/url
                       (some #(when (re-find #"(scittle/[0-9a-f]+|scittle)/src" %) %))
                       fs/parent))]
    (when building-outside-scittle?
      (fs/copy (fs/file scittle-dir "shadow-cljs.edn") "shadow-cljs.edn"))
    (let [cmd (build-cmd cmd (str scittle-dir))]
      (println "> clojure" cmd)
      (clojure {:extra-env {"SCI_ELIDE_VARS" "true"}} cmd))
    (when building-outside-scittle?
      (fs/delete "shadow-cljs.edn"))))

(defn build
  "Build scittle shadow builds using clojure cmd and commandline args. Features on
  classpath are automatically added.

  Options:

  * :action - compile action, defaults to release, but may also be compile or watch"
  [{:keys [action
           args] :or {action "release"}}]
  (build* (format "-M -m shadow.cljs.devtools.cli --force-spawn %s main %s" action (str/join " " args)))
  (when (= "release" action)
    (println "Also building dev release build")
    (build* (format "-M -m shadow.cljs.devtools.cli --force-spawn %s main %s %s"
                    action
                    "--config-merge '{:compiler-options {:optimizations :simple
                                                         :pretty-print true
                                                         :pseudo-names true}
                                      :output-dir \"resources/public/js/dev\"
                                      :modules {:scittle.cljs-devtools {:entries [scittle.cljs-devtools]
                                                                        :depends-on #{:scittle}}}}'"
                    (str/join " " args)))))
