import request from "@/utils/request";

export default {
  getUserList(searchModel) {
    return request({
      url: "/user/list",
      method: "get",
      params: {
        pageNo: searchModel.pageNo,
        pageSize: searchModel.pageSize,
        username: searchModel.username,
        phone: searchModel.phone,
        isFrequent: searchModel.isFrequent,
      },
    });
  },
  addUser(user) {
    return request({
      url: "/user",
      method: "post",
      data: user,
    });
  },
  updateUser(user) {
    return request({
      url: "/user",
      method: "put",
      data: user,
    });
  },
  saveUser(user) {
    if (user.id == null && user.id == undefined) {
      return this.addUser(user);
    }
    return this.updateUser(user);
  },
  getUserById(id) {
    return request({
      //url: '/user/' + id,
      url: `/user/${id}`,
      method: "get",
    });
  },
  deleteUserById(id) {
    return request({
      url: `/user/${id}`,
      method: "delete",
    });
  },
  // 下载导入模板
  downloadTemplate() {
    return request({
      url: `/user/importTemplate`,
      method: "get",
      responseType: "blob",
    });
  },
  // 导出模板
  downloadFile(searchModel) {
    return request({
      url: `/user/export`,
      method: "get",
      responseType: "blob",
      params: {
        pageNo: searchModel.pageNo,
        pageSize: searchModel.pageSize,
        username: searchModel.username,
        phone: searchModel.phone,
        isFrequent: searchModel.isFrequent,
      },
    });
  },
  updateUserFrequent(id) {
    return request({
      url: `/user/editIsFrequentById`,
      method: "post",
      params: { id: id },
    });
  },
};
