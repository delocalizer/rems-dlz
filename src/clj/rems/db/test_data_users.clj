(ns rems.db.test-data-users
  (:require [clojure.test :refer :all]
            [rems.application.approver-bot :as approver-bot]
            [rems.application.bona-fide-bot :as bona-fide-bot]
            [rems.application.rejecter-bot :as rejecter-bot]
            [rems.application.expirer-bot :as expirer-bot]))

(def +bot-users+
  {:approver-bot approver-bot/bot-userid
   :bona-fide-bot bona-fide-bot/bot-userid
   :rejecter-bot rejecter-bot/bot-userid
   :expirer-bot expirer-bot/bot-userid})

(def +bot-user-data+
  {approver-bot/bot-userid {:eppn approver-bot/bot-userid :commonName "Approver Bot"}
   bona-fide-bot/bot-userid {:eppn bona-fide-bot/bot-userid :commonName "Bona Fide Bot"}
   rejecter-bot/bot-userid {:eppn rejecter-bot/bot-userid :commonName "Rejecter Bot"}
   expirer-bot/bot-userid {:eppn expirer-bot/bot-userid :commonName "Expirer Bot"}})

(def +fake-users+
  {:applicant1 "alice"
   :applicant2 "malice"
   :approver1 "developer"
   :approver2 "handler"
   :organization-owner1 "organization-owner1"
   :organization-owner2 "organization-owner2"
   :owner "owner"
   :reporter "reporter"
   :reviewer "carl"
   :roleless1 "elsa"
   :roleless2 "frank"
   :johnsmith "johnsmith"
   :jillsmith "jillsmith"})

(def +fake-user-data+
  {"developer" {:eppn "developer" :mail "developer@example.com" :commonName "Developer" :nickname "The Dev"}
   "alice" {:eppn "alice" :mail "alice@example.com" :commonName "Alice Applicant" :organizations [{:organization/id "default"}] :nickname "In Wonderland" :researcher-status-by "so"}
   "malice" {:eppn "malice" :mail "malice@example.com" :commonName "Malice Applicant" :twinOf "alice" :other "Attribute Value" :mappings {"alt-id" "malice-alt-id"}}
   "handler" {:eppn "handler" :mail "handler@example.com" :commonName "Hannah Handler" :mappings {"alt-id" "handler-alt-id"}}
   "carl" {:eppn "carl" :mail "carl@example.com" :commonName "Carl Reviewer" :mappings {"alt-id" "carl-alt-id"}}
   "elsa" {:eppn "elsa" :mail "elsa@example.com" :commonName "Elsa Roleless" :mappings {"alt-id" "elsa-alt-id"}}
   "frank" {:eppn "frank" :mail "frank@example.com" :commonName "Frank Roleless" :organizations [{:organization/id "frank"}]}
   "organization-owner1" {:eppn "organization-owner1" :mail "organization-owner1@example.com" :commonName "Organization Owner 1"}
   "organization-owner2" {:eppn "organization-owner2" :mail "organization-owner2@example.com" :commonName "Organization Owner 2" :organizations [{:organization/id "organization2"}]}
   "owner" {:eppn "owner" :mail "owner@example.com" :commonName "Owner"}
   "reporter" {:eppn "reporter" :mail "reporter@example.com" :commonName "Reporter"}
   "johnsmith" {:eppn "johnsmith" :commonName "John Smith" :mail "john.smith@example.com" :mappings {"identity1" "johnsmith" "identity2" "smith"}}
   "jillsmith" {:eppn "jillsmith" :commonName "Jill Smith" :mail "jill.smith@example.com" :mappings {"identity1" "jillsmith" "identity2" "smith"}}})

(def +fake-id-data+
  {"developer" {:sub "developer" :email "developer@example.com" :name "Developer" :nickname "The Dev"}
   "alice" {:sub "alice" :email "alice@example.com" :name "Alice Applicant" :organizations [{:organization/id "default"}] :nickname "In Wonderland"}
   "malice" {:sub "malice" :email "malice@example.com" :name "Malice Applicant" :twinOf "alice" :other "Attribute Value"}
   "handler" {:sub "handler" :email "handler@example.com" :name "Hannah Handler"}
   "carl" {:sub "carl" :email "carl@example.com" :name "Carl Reviewer"}
   "elsa" {:sub "elsa" :email "elsa@example.com" :name "Elsa Roleless"}
   "frank" {:sub "frank" :email "frank@example.com" :name "Frank Roleless" :organizations [{:organization/id "frank"}]}
   "organization-owner1" {:sub "organization-owner1" :email "organization-owner1@example.com" :name "Organization Owner 1"}
   "organization-owner2" {:sub "organization-owner2" :email "organization-owner2@example.com" :name "Organization Owner 2" :organizations [{:organization/id "organization2"}]}
   "owner" {:sub "owner" :email "owner@example.com" :name "Owner"}
   "reporter" {:sub "reporter" :email "reporter@example.com" :name "Reporter"}
   "elixir-alice" {:old_sub "alice" :sub "elixir-alice" :email "alice@elixir-europe.org" :name "Alice Applicant (Elixir)" :organizations [{:organization/id "default"}] :nickname "In Wonderland"}
   "johnsmith" {:sub "johnsmith" :name "John Smith" :email "john.smith@example.com" :mappings {"identity1" "johnsmith" "identity2" "smith"}}
   "jillsmith" {:sub "jillsmith" :name "Jill Smith" :email "jill.smith@example.com" :mappings {"identity1" "jillsmith" "identity2" "smith"}}
   "user-has-no-name" {:sub "user-has-no-name" :email "new-user@example.com"}
   "user-has-no-email" {:sub "user-has-no-email" :name "User-Has No-Email"}
   "user-has-nothing" {:sub "user-has-nothing"}
   "new-user" {:sub "new-user" :email "new-user@example.com" :name "New User"}})

(def +fake-user-info+
  {"alice" {:researcher-status-by "so"}
   "elixir-alice" {:researcher-status-by "so"}})

(def +user-descriptions+
  [{:group "Applicants"
    :users [{:userid "alice"
             :description "Alice is a very active user, an applicant, with multiple applications already drafted and even submitted for processing."}
            {:userid "elsa"
             :description "Elsa is a normal user, an applicant, who didn't do anything yet."}
            {:userid "frank"
             :description "Frank is a normal user, an applicant, who didn't do anything yet."}]}

   {:group "Experts"
    :users [{:userid "handler"
             :description "Handler is the user who is responsible for processing applications after they have been sent. They have the permissions given to them by the owner. The REMS model is such that handlers, in general, have a lot of power and flexibility in the handling process."}
            {:userid "owner"
             :description "Owner is the user who owns all the resources and manages them in the administration. They can create the catalogue of resources, workflows, forms etc. They can also delegate this power to other users and set who handles which applications, but aren't necessarily the person who does this work themselves."}
            {:userid "carl"
             :description "Carl is an expert, who is sometimes asked to review applications, thus taking a small part in the handling process."}
            {:userid "reporter"
             :description "Reporter is an administrative users, who is only interested in counting beans and applications, having no part in the actual handling."}]}

   {:group "Special Users"
    :users [{:userid "elixir-alice"
             :description "Alternate identity of Alice, which they can also use to log in and see the same applications."}
            {:userid "malice"
             :description "A scammer who tries to fool everyone to think they are actually Alice."}
            {:userid "developer"
             :description "Another handler, that has a lot of permissions."}
            {:userid "organization-owner1"
             :description "An owner of many organizations, who can manage only their items."}
            {:userid "organization-owner2"
             :description "An owner of many organizations, who can manage only their items."}
            {:userid "johnsmith"
             :description "John and his wife Jill share an external identity (\"smith\"), so they will have difficulty using the API with it."}
            {:userid "jillsmith"
             :description "Jill and her wife John share an external identity (\"smith\"), so they will have difficulty using the API with it."}
            {:userid "user-has-no-name"
             :description "A user who unfortunately has no name defined in the Identity Provider data."}
            {:userid "user-has-no-email"
             :description "A user who unfortunately has no email defined in the Identity Provider data."}
            {:userid "user-has-nothing"
             :description "A user who unfortunately has no name or email defined in the Identity Provider data."}
            {:userid "new-user"
             :description "A user who isn't found in REMS yet. At login they will be created."}]}])

(def +demo-users+
  {:applicant1 "RDapplicant1@funet.fi"
   :applicant2 "RDapplicant2@funet.fi"
   :approver1 "RDapprover1@funet.fi"
   :approver2 "RDapprover2@funet.fi"
   :reviewer "RDreview@funet.fi"
   :organization-owner1 "RDorganizationowner1@funet.fi"
   :organization-owner2 "RDorganizationowner2@funet.fi"
   :owner "RDowner@funet.fi"
   :reporter "RDdomainreporter@funet.fi"})

(def +demo-user-data+
  {"RDapplicant1@funet.fi" {:eppn "RDapplicant1@funet.fi" :mail "RDapplicant1.test@test_example.org" :commonName "RDapplicant1 REMSDEMO1" :organizations [{:organization/id "default"}]}
   "RDapplicant2@funet.fi" {:eppn "RDapplicant2@funet.fi" :mail "RDapplicant2.test@test_example.org" :commonName "RDapplicant2 REMSDEMO"}
   "RDapprover1@funet.fi" {:eppn "RDapprover1@funet.fi" :mail "RDapprover1.test@rems_example.org" :commonName "RDapprover1 REMSDEMO"}
   "RDapprover2@funet.fi" {:eppn "RDapprover2@funet.fi" :mail "RDapprover2.test@rems_example.org" :commonName "RDapprover2 REMSDEMO"}
   "RDreview@funet.fi" {:eppn "RDreview@funet.fi" :mail "RDreview.test@rems_example.org" :commonName "RDreview REMSDEMO"}
   "RDowner@funet.fi" {:eppn "RDowner@funet.fi" :mail "RDowner.test@test_example.org" :commonName "RDowner REMSDEMO"}
   "RDorganizationowner1@funet.fi" {:eppn "RDorganizationowner1@funet.fi" :mail "RDorganizationowner1.test@test_example.org" :commonName "RDorganizationowner1 REMSDEMO" :organizations [{:organization/id "organization1"}]}
   "RDorganizationowner2@funet.fi" {:eppn "RDorganizationowner2@funet.fi" :mail "RDorganizationowner2.test@test_example.org" :commonName "RDorganizationowner2 REMSDEMO" :organizations [{:organization/id "organization2"}]}
   "RDdomainreporter@funet.fi" {:eppn "RDdomainreporter@funet.fi" :mail "RDdomainreporter.test@test_example.org" :commonName "RDdomainreporter REMSDEMO"}})

(def +demo-id-data+
  {"RDapplicant1@funet.fi" {:sub "RDapplicant1@funet.fi" :email "RDapplicant1.test@test_example.org" :name "RDapplicant1 REMSDEMO1" :organizations [{:organization/id "default"}]}
   "RDapplicant2@funet.fi" {:sub "RDapplicant2@funet.fi" :email "RDapplicant2.test@test_example.org" :name "RDapplicant2 REMSDEMO"}
   "RDapprover1@funet.fi" {:sub "RDapprover1@funet.fi" :email "RDapprover1.test@rems_example.org" :name "RDapprover1 REMSDEMO"}
   "RDapprover2@funet.fi" {:sub "RDapprover2@funet.fi" :email "RDapprover2.test@rems_example.org" :name "RDapprover2 REMSDEMO"}
   "RDreview@funet.fi" {:sub "RDreview@funet.fi" :email "RDreview.test@rems_example.org" :name "RDreview REMSDEMO"}
   "RDowner@funet.fi" {:sub "RDowner@funet.fi" :email "RDowner.test@test_example.org" :name "RDowner REMSDEMO"}
   "RDorganizationowner1@funet.fi" {:sub "RDorganizationowner1@funet.fi" :email "RDorganizationowner1.test@test_example.org" :name "RDorganizationowner1 REMSDEMO" :organizations [{:organization/id "organization1"}]}
   "RDorganizationowner2@funet.fi" {:sub "RDorganizationowner2@funet.fi" :email "RDorganizationowner2.test@test_example.org" :name "RDorganizationowner2 REMSDEMO" :organizations [{:organization/id "organization2"}]}
   "RDdomainreporter@funet.fi" {:sub "RDdomainreporter@funet.fi" :email "RDdomainreporter.test@test_example.org" :name "RDdomainreporter REMSDEMO"}})

(def +demo-user-info+
  {})

(def +oidc-users+
  {:applicant1 "WHFS36UEZD6TNURJ76WYLSVDCUUENOOF"
   :applicant2 "C567LI5QAACWKC7YYA74BJ2X7DH7EEYI"
   :approver1 "EKGFNAAGCHIQ5ERUUFS2RCZ44IHYZPEA"
   :approver2 "7R3JYB32PL3EPVD34RWIAWDZSEOXW4OQ"
   :reviewer "F3OJL757ACT4QXVXZZ4F7VG6HQGBEC4M"
   :reporter "JOBDHBMX4EFXQC5IPQVXPP4FFWJ6XQYL"
   :organization-owner1 "W6OKPQGANG6QK54GRF7AOOGMZL7M6IVH"
   :organization-owner2 "D4ZJM7XNXKGFQABRQILDI6EYHLJRLYSF"
   :owner "BACZQAPVWBDJ2OXLKT2WWW5LT5LV6YR4"})

(def +oidc-user-data+
  {"WHFS36UEZD6TNURJ76WYLSVDCUUENOOF" {:eppn "WHFS36UEZD6TNURJ76WYLSVDCUUENOOF" :mail "RDapplicant1@mailinator.com" :commonName "RDapplicant1 REMSDEMO1" :organizations [{:organization/id "default"}]}
   "C567LI5QAACWKC7YYA74BJ2X7DH7EEYI" {:eppn "C567LI5QAACWKC7YYA74BJ2X7DH7EEYI" :mail "RDapplicant2@mailinator.com" :commonName "RDapplicant2 REMSDEMO"}
   "EKGFNAAGCHIQ5ERUUFS2RCZ44IHYZPEA" {:eppn "EKGFNAAGCHIQ5ERUUFS2RCZ44IHYZPEA" :mail "RDapprover1@mailinator.com" :commonName "RDapprover1 REMSDEMO"}
   "7R3JYB32PL3EPVD34RWIAWDZSEOXW4OQ" {:eppn "7R3JYB32PL3EPVD34RWIAWDZSEOXW4OQ" :mail "RDapprover2@mailinator.com" :commonName "RDapprover2 REMSDEMO"}
   "F3OJL757ACT4QXVXZZ4F7VG6HQGBEC4M" {:eppn "F3OJL757ACT4QXVXZZ4F7VG6HQGBEC4M" :mail "RDreview@mailinator.com" :commonName "RDreview REMSDEMO"}
   "JOBDHBMX4EFXQC5IPQVXPP4FFWJ6XQYL" {:eppn "JOBDHBMX4EFXQC5IPQVXPP4FFWJ6XQYL" :mail "RDdomainreporter@mailinator.com" :commonName "RDdomainreporter REMSDEMO"}
   "W6OKPQGANG6QK54GRF7AOOGMZL7M6IVH" {:eppn "W6OKPQGANG6QK54GRF7AOOGMZL7M6IVH" :mail "RDorganizationowner1@mailinator.com" :commonName "RDorganizationowner1 REMSDEMO" :organizations [{:organization/id "organization1"}]}
   "D4ZJM7XNXKGFQABRQILDI6EYHLJRLYSF" {:eppn "D4ZJM7XNXKGFQABRQILDI6EYHLJRLYSF" :mail "RDorganizationowner2@mailinator.com" :commonName "RDorganizationowner2 REMSDEMO" :organizations [{:organization/id "organization2"}]}
   "BACZQAPVWBDJ2OXLKT2WWW5LT5LV6YR4" {:eppn "BACZQAPVWBDJ2OXLKT2WWW5LT5LV6YR4" :mail "RDowner@mailinator.com" :commonName "RDowner REMSDEMO"}})
