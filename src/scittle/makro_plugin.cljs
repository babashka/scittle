(ns scittle.makro-plugin
  (:require
   [sci.core :as sci]
   [scittle.core :as scittle]))

(defn add-low-fn [_env _form x y] `(str "_" ~x ~y))
(defn ^:macro add-low-makro [_env _form x y] (add-low-fn _env _form x y))

(def rns (sci/create-ns 'makro-plugin.core nil))

(def makro-plugin-namespace
  {'add-low-fn (sci/copy-var add-low-fn rns)
   'add-low-makro (sci/copy-var add-low-makro rns)})

(scittle/register-plugin!
  ::makro-plugin
  {:namespaces {'makro-plugin.core makro-plugin-namespace}})
