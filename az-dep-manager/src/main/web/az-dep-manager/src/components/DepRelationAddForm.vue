<template>
  <a-modal
    :visible="visible"
    :title="'新增依赖'"
    okText="确认"
    cancel-text="取消"
    @cancel="handleCancel"
    @ok="handleOk"
    @afterClose="handleCancel"
  >
    <a-form class="dep-relation-add-form" :form="form">
      <a-row>
        <a-col :key="'project_id'">
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
        <a-col :key="'flow_id'">
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
      </a-row>
      <a-row>
        <a-col :key="'depended_project_id'">
          <a-form-item :label="`前置项目`">
            <a-select
              show-search
              allowClear
              placeholder="选择前置项目"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['dependedProjectId']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.id">{{ d.name }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'depended_flow_id'">
          <a-form-item :label="`前置工作流`">
            <a-select
              show-search
              allowClear
              placeholder="选择前置工作流"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="handleDepedFlowSearch"
              :not-found-content="null"
              v-decorator="['dependedFlowId']"
            >
              <a-select-option v-for="flowId in depedFlows" :key="flowId">{{ flowId }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>
<script>
import services from "../services";
export default {
  name: "DepRelationAddForm",
  props: {
    visible: Boolean,
    okCallBack: Function,
    cancelCallBack: Function
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      depedProjects: [],
      depedFlows: [],
      projects: [],
      flows: []
    };
  },
  computed: {},
  methods: {
    handleDepedProjectSearch(value) {
      services.searchProjectByName(
        value,
        res => {
          if (res.data.code!=0) {
            this.$message.error(res.data.message||"Something error");
            return;
          }
          this.depedProjects = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleDepedProjectChange(value, option) {
      //update depedFlows
      services.getFlowsByProject(
        value,
        res => {
          if (res.data.code!=0) {
            this.$message.error(res.data.message||"Something error");
            this.flows=[]
            return;
          }
          this.depedFlows = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },

    handleDepedFlowSearch(value, option) {
      return option.componentOptions.children[0].text.indexOf(value) >= 0;
    },
    handleProjectSearch(value) {
      services.searchUserProjectByName(
        value,
        res => {
          if (res.data.code!=0) {
            this.$message.error(res.data.message||"Something error");
            return;
          }
          this.projects = res.data.data;
        },
        res=>{this.$message.error("Error")}
      );
    },
    handleProjectChange(value, option) {
      //update flows
      services.getFlowsByProject(
        value,
        res => {
          if (res.data.code!=0) {
            this.$message.error(res.data.message||"Something error");
            this.flows=[]
            return;
          }
          this.flows = res.data.data;
        },
        res=>{this.$message.error("Error")}
      );
    },
    handleFlowSearch(value, option) {
      return option.componentOptions.children[0].text.indexOf(value) >= 0;
    },
    handleOk(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        console.log("error", error);
        console.log("Received values of form: ", values);
      });
      let params = this.form.getFieldsValue();
      services.addFlowRelation(
        params,
        res => {
          if (res.data.code!=0) {
            this.$message.error(res.data.message||"Something error");
            return;
          }
          this.tableData = res.data;
        },
        res=>{this.$message.error("Error")}
      );
      if (this.okCallBack) {
        this.okCallBack();
      }
    },
    handleCancel() {
      if (this.cancelCallBack) {
        this.cancelCallBack();
      }
    }
  }
};
</script>
