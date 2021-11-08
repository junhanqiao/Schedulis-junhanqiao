<template>
  <a-layout id="components-layout-demo-top-side-2">
    <a-layout-header class="header">
      <div class="logo" />
      <a-menu
        theme="dark"
        mode="horizontal"
        :default-selected-keys="['2']"
        :style="{ lineHeight: '64px' }"
        @click="handleNavClick"
      >
        <a-menu-item key="homepage">首页</a-menu-item>
        <a-menu-item key="index">项目</a-menu-item>
        <a-menu-item key="schedule">定时调度</a-menu-item>
        <a-menu-item key="dep">依赖管理</a-menu-item>
        <a-menu-item key="executor">正在运行</a-menu-item>
        <a-menu-item key="history">执行历史</a-menu-item>
        <a-menu-item key="system">系统管理</a-menu-item>
        <a-sub-menu key="login">
          <span slot="title">
            <a-icon type="down" />
            <span>{{this.loginUserInfo.userId}}</span>
          </span>
          <a-menu-item key="logout">登出</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-header>
    <a-layout>
      <a-layout style="padding: 0 24px 24px">
        <a-layout-content
          :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
        >
          <a-tabs default-active-key="depManage" :tab-position="`left`">
            <a-tab-pane key="depManage" :tab="`依赖管理`">
              <DepRelationManage />
            </a-tab-pane>
            <a-tab-pane key="execManage" :tab="`执行管理`">
              <DepExecManage />
            </a-tab-pane>
          </a-tabs>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </a-layout>
</template>
<script>
import DepRelationManage from "./components/DepRelationManage";
import DepExecManage from "./components/DepExecManage";
import services from "./services";
export default {
  data() {
    return {
      collapsed: false,
      loginUserInfo: {
        userId: null
      }
    };
  },
  components: { DepRelationManage, DepExecManage },
  methods: {
    handleNavClick(value) {
      if ("logout" == value.key) {
        window.location = "/dep?logout";
      } else {
        window.location = value.key;
      }
    }
  },
  created: function() {
    services.loginUserInfo(
      null,
      res => {
        if (res.data.code != 0) {
          this.$message.error(res.data.message || "Something error");
          return;
        }
        this.loginUserInfo = res.data.data;
      },
      res => {
        this.$message.error("Error");
      }
    );
  }
};
</script>

<style>
#components-layout-demo-top-side-2 .logo {
  width: 120px;
  height: 31px;
  background: rgba(255, 255, 255, 0.2);
  margin: 16px 28px 16px 0;
  float: left;
}
</style>
