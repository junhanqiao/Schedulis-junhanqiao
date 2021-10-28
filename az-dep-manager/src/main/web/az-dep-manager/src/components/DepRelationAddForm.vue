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
        <a-col :key="'depended_project_id'">
          <a-form-item :label="`前置项目`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['depended_project_id']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'depended_flow_id'">
          <a-form-item :label="`前置工作流`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['depended_flow_id']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
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
  props:{
      visible:Boolean,
      okCallBack:Function,
      cancelCallBack:Function,
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      depedProjects: [],
      depedFlows: [],
      projects: [],
      flows: [],
    };
  },
  computed: {},
  methods: {
    handleDepedProjectSearch(value) {
      services.searchProjectByName(value, res => {
        this.depedProjects = res.data;
      });
    },
    handleDepedProjectChange(value, option) {
      console.log(value);
      console.log(option);
      //update depedFlows
    },
    handleOk(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        console.log("error", error);
        console.log("Received values of form: ", values);
      });
      services.searchDepRelations("", res => {
        this.tableData = res.data;
      });
      if(this.okCallBack){
          this.okCallBack()
      }
    },
    handleCancel(){
      if(this.cancelCallBack){
          this.cancelCallBack()
      }        
    }
  }
};
</script>
