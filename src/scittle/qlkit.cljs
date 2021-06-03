(ns scittle.qlkit
  (:require [qlkit.core :as ql :refer-macros [defcomponent*]]
            [sci.core :as sci]
            [scittle.core :as scittle]
            [sablono.core :as html :refer-macros [html html*]]
            [sablono.interpreter :as si]))

(def qns (sci/create-ns 'qlkit.core nil))

(defn ^:macro defcomponent-fn [_form _env comp-name [_ q] [_ [_ m _] [_ h]]]
  `(defcomponent* ~comp-name
    (query ~q)
    (render [this ~m state]
            (html ~h)))

  #_(defcomponent* comp-name (query q) (render r (html [:p "hu"])))

  )

(defn ^:macro defcomponent+ [form env nam & bodies]
  (apply ql/defcomponent+ form env nam bodies))

(defn random-uuid-fn []
  (random-uuid))

(def qlkit-namespace
  {'add-class (sci/copy-var ql/add-class qns)
   ;; 'defcomponent* (sci/copy-var defcomponent-fn qns)
   'defcomponent* (sci/copy-var defcomponent+ qns)
   'transact!* (sci/copy-var ql/transact!* qns)
   'update-state!* (sci/copy-var ql/update-state!* qns)
   'create-instance (sci/copy-var ql/create-instance qns)
   'get-query (sci/copy-var ql/get-query qns)
   'parse-children (sci/copy-var ql/parse-children qns)
   'parse-children-remote (sci/copy-var ql/parse-children-remote qns)
   'parse-children-sync (sci/copy-var ql/parse-children-sync qns)
   'mount (sci/copy-var ql/mount qns)

   'classes (sci/copy-var ql/classes qns)
   'random-uuid (sci/copy-var random-uuid-fn qns)})

(def sns (sci/create-ns 'sablono.core nil))

(defn ^:macro html+ [form env html-form]
  (html* [:p "in sablono"]))

(def sablono-core-namespace
  {'html (sci/copy-var html+ sns)})

(def sins (sci/create-ns 'sablono.interpreter nil))

(def sablono-interpreter-ns
  {'interpret (sci/copy-var si/interpret sins)})

(scittle/register-plugin!
  ::qlkit
  {:namespaces {'qlkit.core qlkit-namespace
                'sablono.core sablono-core-namespace
                'sablono.interpreter sablono-interpreter-ns}})
