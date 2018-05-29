var myCharts1=null;
var timer=null;

$(function(){
    //初始化图形显示界面eCharts
    myCharts1 = echarts.init(document.getElementById('result'));
    //设置定时器
    timer=setInterval(getResult,1000);
});

function getResult() {
    $.post("/getResult", {"subject": $("#subject").text()}, function (data) {
        var subjects=data.subjects;
        if(subjects!=null ? subjects.length > 1 : false) {
            clearInterval(timer);//清除定时器
            $("#message").empty();

            var list = "";
            $.each(subjects, function (index, item) {
                list += '<option value="' + item + '">' + item + '</option>'
            })
            $("#message").html('<select id="subjects">' + list + '</select><input type="button" onclick="selectSubject()" value="确定"></input>');
        }else {
            var runningCount = data.runningCount;
            var pendingCount = data.pendingCount;
            var successCount = data.successCount;
            var failedCount = data.failedCount;
            var finished = data.finished;
            var emails = data.failedEmail;

            var options = {
                title: {
                    text: '邮件发送状态',
                    subtext: '待发:' + pendingCount + ";正发:" + runningCount + ";成功:" + successCount + ";失败:" + failedCount,
                    x: 'center'
                },
                tooltip: {
                    trigger: 'item',
                    formatter: "{a} <br/>{b} : {c} ({d}%)"
                },
                legend: {
                    orient: 'vertical',
                    left: 'left',
                    data: ['待发送', '正在发送', '成功', '失败']
                },
                series: [
                    {
                        name: '状态',
                        type: 'pie',
                        radius: '55%',
                        center: ['50%', '50%'],
                        data: [
                            {value: pendingCount, name: '待发送', itemStyle: {color: '#B2DFEE'}},
                            {value: runningCount, name: '正在发送', itemStyle: {color: '#1E90FF'}},
                            {value: successCount, name: '成功', itemStyle: {color: '#32CD32'}},
                            {value: failedCount, name: '失败', itemStyle: {color: '#c23531'}}
                        ],
                        itemStyle: {
                            emphasis: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }

                        }
                    }
                ]
            };
            myCharts1.setOption(options, true);

            $("#message").empty();
            if (failedCount > 0) {
                var html = "";
                $.each(emails, function (index, item) {
                    html += '<tr><td>' + item.name + '</td>' +
                        '<td>' + item.to + '</td>' +
                        '<td>' + item.message + '</td></tr>';
                });
                $("#message").html('<table><tr><th>姓名</th><th>邮件</th><th>失败原因</th></tr>' + html
                    + '<tr><td colspan="3" class="noborder"><input type="button" id="reSendBtn" value="重发" disabled="disabled" /></td></tr></table>');

                $("#reSendBtn").click(function () {
                    $.post("/reSend", {"subject": $("#subject").text()}, function (data) {
                        if (data > 0) {
                            alert("重发成功，请注意发送状态变化")
                            timer = setInterval(getResult, 1000);
                            $("#reSendBtn").attr("disabled", true);
                        }else{
                            alert("记录已经删除,重发失败")
                            $("#reSendBtn").attr("disabled", true);
                        }
                    })
                })
            }

            if (finished == true) {
                clearInterval(timer);
                $("#reSendBtn").removeAttr("disabled");
                if($("#result").find("#deleteBtn").length<=0) {
                    $("#result").append('<input type="button" id="deleteBtn" value="删除工资条记录"/>');
                    $("#deleteBtn").click(function () {
                        $("#deleteBtn").attr("disabled", true);
                        $.post("/delete", {"subject": $("#subject").text()}, function (data) {
                            alert(data);
                        })
                    });
                }
            }

            //设置主题值
            $("#subject").text(subjects!=null?subjects[0]:"");
            }
    });
};

function selectSubject(){
    var select=document.getElementById("subjects");
    var index = select.selectedIndex; // 选中索引
    var value = select.options[index].value; // 选中值
    $("#subject").text(value);
    timer = setInterval(getResult, 1000);//请求显示发送结果
}