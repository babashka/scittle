(ns scittle.qlkit
  (:require [qlkit.core :as ql]
            [sablono.interpreter :as si]
            [cljs.reader :refer [read-string]]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def qns (sci/create-ns 'qlkit.core nil))

(defn ^:macro defcomponent* [form env nam & bodies]
  (apply ql/defcomponent+ form env nam bodies))

(def qlkit-namespace
  {'add-class (sci/copy-var ql/add-class qns)
   'defcomponent* (sci/copy-var defcomponent* qns)
   'transact!* (sci/copy-var ql/transact!* qns)
   'update-state!* (sci/copy-var ql/update-state!* qns)
   'create-instance (sci/copy-var ql/create-instance qns)
   'get-query (sci/copy-var ql/get-query qns)
   'parse-children (sci/copy-var ql/parse-children qns)
   'parse-children-remote (sci/copy-var ql/parse-children-remote qns)
   'parse-children-sync (sci/copy-var ql/parse-children-sync qns)
   'mount (sci/copy-var ql/mount qns)

   'random-uuid (sci/copy-var random-uuid qns)
   'read-string (sci/copy-var read-string qns)})

(def sins (sci/create-ns 'sablono.interpreter nil))

(def sablono-interpreter-ns
  {'interpret (sci/copy-var si/interpret sins)})

(scittle/register-plugin!
  ::qlkit
  {:namespaces {'qlkit.core qlkit-namespace
                'sablono.interpreter sablono-interpreter-ns}})
