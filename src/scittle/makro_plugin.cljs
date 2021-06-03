(ns scittle.makro-plugin
  (:require-macros [scittle.makros :refer
                   [add-slash-makro add-c-makro]])
  (:require
   [sci.core :as sci]
   [scittle.core :as scittle]
   [scittle.makros :as sm]))

(.log js/console (add-slash-makro 1 2))
(.log js/console (add-c-makro 5 6))

(defn add-low-fn [form env x y]
  `(str "_ form: " ~(str form) " env: " ~(str env) ~x ~y))
(defn ^:macro add-low-sci-makro [form env x y] (add-low-fn form env x y))
(defn ^:macro add-slash-sci-makro [_form _env x y] (add-slash-makro x y))
(defn ^:macro add-c-sci-makro [form env x y] (sm/add-c-fn form env x y))

(def rns (sci/create-ns 'scittle.makros nil))

(def scittle-makros-namespace
  {'add-low-fn (sci/copy-var add-low-fn rns)
   'add-low-sci-makro (sci/copy-var add-low-sci-makro rns)
   'make-string (sci/copy-var sm/make-string rns)
   'add-slash-sci-makro (sci/copy-var add-slash-sci-makro rns)
   'add-c-sci-makro (sci/copy-var add-c-sci-makro rns)
   })

(scittle/register-plugin!
  ::makro-plugin
  {:namespaces {'scittle.makros scittle-makros-namespace}})
