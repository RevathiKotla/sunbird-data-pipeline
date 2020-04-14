package org.sunbird.dp.fixture

import org.joda.time.DateTime

object EventFixture {
  
  
  val dialcodedata1 = """{"ets":1585728609983,"channel":"0124784842112040965","transactionData":{"properties":{"dialcodes":{"ov":null,"nv":["X3J6W1"]},"SYS_INTERNAL_LAST_UPDATED_ON":{"ov":"2020-04-01T08:10:09.875+0000","nv":"2020-04-01T08:10:09.942+0000"}}},"mid":"29b0e66d-94c7-4cc4-b720-658128ab6364","label":"Testing course1","nodeType":"DATA_NODE","userId":"ANONYMOUS","createdOn":"2020-04-01T08:10:09.983+0000","objectType":"Content","nodeUniqueId":"do_2129902851973693441453","requestId":null,"operationType":"UPDATE","nodeGraphId":215207,"graphId":"domain"}""";
  val deviceCacheData2 = """{"user_declared_state":"Maharashtra","state_custom":"Maharashtra","devicespec":"{\"scrn\":\"5.46\",\"camera\":\"\",\"idisk\":\"25.44\",\"os\":\"Android 9\",\"id\":\"45f32f48592cb9bcf26bef9178b7bd20abe24932\",\"sims\":\"-1\",\"cpu\":\"abi: armeabi-v7a processor\t: 0 \",\"webview\":\"79.0.3945.116\",\"edisk\":\"25.42\",\"make\":\"Samsung SM-J400F\"}","uaspec":"{\"agent\":\"UNKNOWN\",\"ver\":\"UNKNOWN\",\"system\":\"Android\",\"raw\":\"Dalvik/2.1.0 (Linux; U; Android 9; SM-J400F Build/PPR1.180610.011)\"}","city":"Mumbai","country_code":"IN","firstaccess":"1578972432419","country":"India","country_name":"India","state":"Maharashtra","continent_name":"Asia","state_code":"MH","fcm_token":"d3ddT88xXLI:APA91bF9lJ4eH8tshAPgKiiZ3hL3sbib0pUN2I388T58oFDxUBQ2WKKuvtBga6iKiOPrgssNKLs4QBjZxE_BbtdGdO0gPdFPataEeXshgYxMKC0VT-oyjrNIZKdKkybQoyichBCiokTD","producer":"sunbirddev.diksha.app","district_custom":"Mumbai","user_declared_district":"Raigad","state_code_custom":"27"}""";
  

  val contentData1 = """{"ets":1585729640345,"channel":"012550822176260096119","transactionData":{"properties":{"ownershipType":{"ov":null,"nv":["createdBy"]},"code":{"ov":null,"nv":"org.sunbird.kgcdDt"},"channel":{"ov":null,"nv":"012550822176260096119"},"description":{"ov":null,"nv":"Enter description for TextBook"},"organisation":{"ov":null,"nv":["diksha_ntptest_org"]},"language":{"ov":null,"nv":["English"]},"mimeType":{"ov":null,"nv":"application/vnd.ekstep.content-collection"},"idealScreenSize":{"ov":null,"nv":"normal"},"createdOn":{"ov":null,"nv":"2020-04-01T08:25:24.896+0000"},"appId":{"ov":null,"nv":"staging.diksha.portal"},"contentDisposition":{"ov":null,"nv":"inline"},"lastUpdatedOn":{"ov":null,"nv":"2020-04-01T08:25:24.896+0000"},"contentEncoding":{"ov":null,"nv":"gzip"},"contentType":{"ov":null,"nv":"TextBook"},"dialcodeRequired":{"ov":null,"nv":"No"},"creator":{"ov":null,"nv":"suborg_creator_sun 3"},"createdFor":{"ov":null,"nv":["012550822176260096119"]},"lastStatusChangedOn":{"ov":null,"nv":"2020-04-01T08:25:24.896+0000"},"audience":{"ov":null,"nv":["Learner"]},"IL_SYS_NODE_TYPE":{"ov":null,"nv":"DATA_NODE"},"visibility":{"ov":null,"nv":"Default"},"os":{"ov":null,"nv":["All"]},"consumerId":{"ov":null,"nv":"a9cb3a83-a164-4bf0-aa49-b834cebf1c07"},"mediaType":{"ov":null,"nv":"content"},"osId":{"ov":null,"nv":"org.ekstep.quiz.app"},"version":{"ov":null,"nv":2},"versionKey":{"ov":null,"nv":"1585729524896"},"idealScreenDensity":{"ov":null,"nv":"hdpi"},"license":{"ov":null,"nv":"CC BY 4.0"},"framework":{"ov":null,"nv":"ekstep_ncert_k-12"},"createdBy":{"ov":null,"nv":"7a6b150c-08be-4e31-8f69-7fc4b479e61d"},"compatibilityLevel":{"ov":null,"nv":1},"IL_FUNC_OBJECT_TYPE":{"ov":null,"nv":"Content"},"name":{"ov":null,"nv":"BookA"},"IL_UNIQUE_ID":{"ov":null,"nv":"do_2129902962679398401472"},"resourceType":{"ov":null,"nv":"Book"},"status":{"ov":null,"nv":"Draft"}}},"mid":"46e30999-1358-4c93-a03f-eebfdf83eb15","label":"BookA","nodeType":"DATA_NODE","userId":"ANONYMOUS","createdOn":"2020-04-01T08:27:20.345+0000","objectType":"Content","nodeUniqueId":"do_2129902962679398401472","requestId":null,"operationType":"CREATE","nodeGraphId":215201,"graphId":"domain"}""";
  val contentCacheData2 = """{"identifier":"do_312526125187809280139353","code":"c361b157-408c-4347-9be3-38d9d4ea2b90","visibility":"Parent","description":"Anction Words","mimeType":"application/vnd.ekstep.content-collection","graph_id":"domain","nodeType":"DATA_NODE","createdOn":"2018-06-15T13:06:56.090+0000","versionKey":"1529068016090","objectType":"Content","dialcodes":["TNPF7T"],"collections":["do_312526125186424832139255"],"name":"Anction Words","lastUpdatedOn":"2018-06-15T13:06:56.090+0000","lastsubmittedon":1571999041881,"lastPublishedOn":"2018-06-15T13:06:56.090+0000","contentType":"TextBookUnit","status":"Draft","node_id":438622.0,"license":"CC BY 4.0"}""";
  val contentCacheData3 = """{"code":"do_312526125187809280139355","channel":"in.ekstep","downloadUrl":"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/14681653089315c3173a3e1.mp3","language":["English"],"mimeType":"application/octet-stream","idealScreenSize":"normal","createdOn":"2016-07-10T15:41:49.111+0000","objectType":"Content","gradeLevel":["Class 1"],"contentDisposition":"inline","contentEncoding":"identity","lastUpdatedOn":"2017-03-10T18:10:00.448+0000","contentType":"Asset","identifier":"do_312526125187809280139355","os":["All"],"visibility":"Default","mediaType":"audio","osId":"org.ekstep.launcher","ageGroup":["5-6"],"graph_id":"domain","nodeType":"DATA_NODE","pkgVersion":1.0,"versionKey":"1489169400448","license":"CC BY 4.0","idealScreenDensity":"hdpi","framework":"NCF","compatibilityLevel":1.0,"name":"do_312526125187809280139355","status":"Live","node_id":65545.0,"size":677510.0}""";
  

  val olderDate = DateTime.now().minusMonths(4).getMillis;
  val futureDate = DateTime.now().plusMonths(1).getMillis;
  



}
