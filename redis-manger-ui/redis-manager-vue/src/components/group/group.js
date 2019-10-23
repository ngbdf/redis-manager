import { store } from "@/vuex/store.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";

export function getGroupList(user) {
      let url = "/group/getGroupList";
      API.post(
        url,
        user,
        response => {
          if (response.data.code == 0) {
            let groupList = response.data.data;
            groupList.forEach(group => {
              group.time = formatTime(group.updateTime);
            });
            console.log(groupList)
            store.dispatch("setGroupList", groupList);
          } else {
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
}