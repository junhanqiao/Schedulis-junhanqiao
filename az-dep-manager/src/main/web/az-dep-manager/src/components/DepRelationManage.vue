<template>
  <div id="dep-relation-manage">
    <a-divider />    
    <DepRelationSearchForm :searchCallBack='searchCallBack'>
        <a-table bordered :data-source="tableData" :columns="columns" :rowKey="`id`" :pagination='false'>
            <template slot="operation" slot-scope="text, record">
                <a-popconfirm
                v-if="tableData.length"
                title="确定删除?"
                @confirm="() => onDelete(record.id)"
                >
                <a href="javascript:;">删除</a>
                </a-popconfirm>
            </template>
        </a-table>
    </DepRelationSearchForm>
  </div>
  
</template>
<script>
import DepRelationSearchForm from './DepRelationSearchForm'
import moment from 'moment'
import services from '../services';
export default {
  name: 'DepRelationManage',  
  components:{DepRelationSearchForm},
  data() {
    return {
      tableData: [],
      columns: [
        {
          title: 'id',
          dataIndex: 'id',
          width: '30%',
          scopedSlots: { customRender: 'id' },
        },
        {
          title: '前置项目',
          dataIndex: 'dependedProjectName',
        },
        {
          title: '前置工作流',
          dataIndex: 'dependedFlowId',
        },
        {
          title: '项目',
          dataIndex: 'projectName',
        },
        {
          title: '工作流',
          dataIndex: 'flowId',
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          customRender:(text, row, index)=>{
            let result="";
            if(row.createTime){
              result=moment(row.createTime).format()
            }
            return result;
          },
        },
        {
          title: '修改时间',
          dataIndex: 'modify_time',
          customRender:(text, row, index)=>{
            let result="";
            if(row.modifyTime){
              result=moment(row.modifyTime).format()
            }
            return result;
          },
        },
        {
          title: '操作',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
        },
      ],      


    };
  },
  computed: {

  },
  methods: {

    searchCallBack(res){
        this.tableData=res.data
    },

    onDelete(key) {
      services.deleteFlowRelation(
        key,
        res=>{
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }          
          const tableData = [...this.tableData];
          this.tableData = tableData.filter(item => item.id !== key);
        },
        res=>{
          this.$message.error("delete failed");
        }
      )

    },

  },
};
</script>
