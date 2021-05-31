(ns scittle.makro-plugin
  (:require
   [sci.core :as sci]
   [scittle.core :as scittle]))

(defn add-low-fn [_env _form x y & zs] `(str "__" ~x ~y ~zs))
(def add-low-makro (with-meta add-low-fn {:sci/macro true}))

(def rns (sci/create-ns 'makro-plugin.core nil))

(def makro-plugin-namespace
  {'add-low-fn (sci/copy-var add-low-fn rns)
   'add-low-makro (sci/copy-var add-low-makro rns)})

(scittle/register-plugin!
  ::makro-plugin
  {:namespaces {'makro-plugin.core makro-plugin-namespace}})
