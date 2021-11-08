<template>
  <div id="dep-relation-search-form">
    <a-form class="dep-relation-add-form" :form="form" @submit="handleSearch">
      <a-row type="flex" justify="center">
        <a-col :key="'project_id'" :span="4">
          <a-form-item :label="`项目`">
            <a-select
              show-search
              allowClear
              placeholder="选择项目"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleProjectSearch"
              @change="handleProjectChange"
              v-decorator="['projectId']"
            >
              <a-select-option v-for="d in projects" :key="d.id">{{ d.name }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'flow_id'" :span="4">
          <a-form-item :label="`工作流`">
            <a-select
              show-search
              allowClear
              placeholder="选择工作流"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="handleFlowSearch"
              :not-found-content="null"
              v-decorator="['flowId']"
            >
              <a-select-option v-for="flowId in flows" :key="flowId">{{ flowId }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'timeIdRange'" :span="4">
          <a-form-item :label="`时间ID`">
            <a-range-picker
              :show-time="{ format: 'HH:mm:ss' }"
              format="YYYY-MM-DD HH:mm:ss"
              :placeholder="['开始', '结束']"
              @change="onTimeIdRangeChange"
            />
          </a-form-item>
        </a-col>
        <a-clo :key="'statuses'" :span="4">
          <a-form-item :label="`实例状态`">
            <a-checkbox-group :options="instanceStatusOptions" v-decorator="['statuses']" />
          </a-form-item>
        </a-clo>
      </a-row>

      <a-row>
        <a-col :span="24" :style="{ textAlign: 'right' }">
          <a-button type="primary" icon="search" html-type="submit">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-col>
      </a-row>
    </a-form>
    <slot></slot>
    <a-pagination
      show-size-changer
      :default-current="1"
      :pageSize="pageSize"
      :current="pageNum"
      :total="total"
      @change="handlePageChange"
      @showSizeChange="handleShowSizeChange"
    />
  </div>
</template>
<script>
import services from "../services";
import moment from "moment";
export default {
  name: "DepExecSearchForm",
  props: {
    searchCallBack: Function
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      projects: [],
      flows: [],
      pageSize: 20,
      pageNum: 1,
      total: 100,
      //add form
      addFormVisible: false,
      startTimeId: null,
      endTimeId: null,
      instanceStatusOptions: [
        { label: "INIT", value: 0 },
        { label: "READY", value: 1 },
        { label: "SUBMITTED", value: 2 },
        { label: "SUCCESS", value: 3 },
        { label: "FAILED", value: 4 }
      ]
    };
  },
  computed: {},
  methods: {
    handleProjectSearch(value) {
      services.searchProjectByName(
        value,
        res => {
          if (res.data.code != 0) {
            this.$message.error(res.data.message || "Something error");
            return;
          }
          this.projects = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleProjectChange(value, option) {
      //update flows
      services.getFlowsByProject(
        value,
        res => {
          if (res.data.code != 0) {
            this.$message.error(res.data.message || "Something error");
            this.flows=[]
            return;
          }
          this.flows = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleFlowSearch(value, option) {
      return option.componentOptions.children[0].text.indexOf(value) >= 0;
    },
    handleSearch(e) {
      e.preventDefault();
      this.doSearch();
    },

    handleReset() {
      this.form.resetFields();
    },
    handlePageChange(page, pageSize) {
      console.log({ page, pageSize });
      this.pageNum = page;
      this.doSearch();
    },
    handleShowSizeChange(page, pageSize) {
      console.log({ page, pageSize });
      this.pageSize = pageSize;
      this.doSearch();
    },
    doSearch() {
      this.form.validateFields((error, values) => {
        if (error) {
          console.log("error", error);
          return;
        }
      });
      let params = this.form.getFieldsValue();
      let pageSize = this.pageSize;
      let pageNum = this.pageNum;
      let startTimeId = this.startTimeId;
      let endTimeId = this.endTimeId;
      params = { ...params, startTimeId, endTimeId, pageSize, pageNum };

      services.searchFlowInstance(
        params,
        res => {
          if (res.data.code != 0) {
            this.$message.error(res.data.message || "Something error");
            return;
          }
          this.total = res.data.total;
          this.searchCallBack(res.data);
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    onTimeIdRangeChange(values, dateStrings) {
      this.startTimeId = dateStrings[0];
      this.endTimeId = dateStrings[1];
    }
  }
};
</script>
