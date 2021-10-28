import Mock from 'mockjs'
var relations=[{
    "id" : 1,
    "depended_project_id" : 1,
    "depended_flow_id" : "test",
    "project_id" : 1,
    "flow_id" : "testDependent",
    "create_time" : "2021-10-13 16:57:24.0",
    "modify_time" : "2021-10-13 16:57:24.0"
},
{
    "id" : 2,
    "depended_project_id" : 1,
    "depended_flow_id" : "test",
    "project_id" : 1,
    "flow_id" : "testDependent1-1",
    "create_time" : null,
    "modify_time" : null
},
{
    "id" : 3,
    "depended_project_id" : 1,
    "depended_flow_id" : "test",
    "project_id" : 1,
    "flow_id" : "testDependent1-2",
    "create_time" : null,
    "modify_time" : null
},
{
    "id" : 4,
    "depended_project_id" : 1,
    "depended_flow_id" : "testDependent1-1",
    "project_id" : 1,
    "flow_id" : "testDependent2-1",
    "create_time" : null,
    "modify_time" : null
},
{
    "id" : 5,
    "depended_project_id" : 1,
    "depended_flow_id" : "testDependent1-2",
    "project_id" : 1,
    "flow_id" : "testDependent2-1",
    "create_time" : null,
    "modify_time" : null
},
{
    "id" : 6,
    "depended_project_id" : 1,
    "depended_flow_id" : "test",
    "project_id" : 2,
    "flow_id" : "whqiao",
    "create_time" : null,
    "modify_time" : null
}
];
Mock.mock('/dep/relations','get',relations);
var projects=[
    {"value":1,"text":"project1"},
    {"value":2,"text":"project2"},
]
Mock.mock('/project/searchProject','get',projects)