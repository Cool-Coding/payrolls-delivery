    var heartflag = false;
    var webSocket = null;
    var tryTime = 0;
    var myCharts1=null;

    $(function(){
        //初始化图形显示界面eCharts
        myCharts1 = echarts.init(document.getElementById('result'));
        initSocket();
    });

    /**
     * 初始化websocket，建立连接
     */
    function initSocket() {
        if (!window.WebSocket) {
            $("#connectStatu").append(getNowFormatDate() + '您的浏览器不支持ws');
            return false;
        }
        webSocket = new WebSocket("ws://localhost/" + $("#subject").text());

        // 收到服务端消息
        webSocket.onmessage = function (msg) {
            if (msg.data == "&") {

            } else {
                var data = $.parseJSON(msg.data);
                var runningCount = data.runningCount;
                var pendingCount = data.pendingCount;
                var successCount = data.successCount;
                var failedCount = data.failedCount;
                var finished = data.finished;
                var emails = data.failedEmail;
                var subjects = data.subjects;

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
                                $("#reSendBtn").attr("disabled", true);
                                $("#deleteBtn").attr("disabled", true);
                            } else {
                                alert("记录已经删除,重发失败")
                                $("#reSendBtn").attr("disabled", true);
                            }
                        })
                    })
                }

                if (finished == true) {
                    $("#reSendBtn").removeAttr("disabled");
                    $("#deleteBtn").removeAttr("disabled");
                    if ($("#result").find("#deleteBtn").length <= 0) {
                        $("#result").append('<input type="button" id="deleteBtn" value="删除工资条记录"/>');
                        $("#deleteBtn").click(function () {
                            $("#deleteBtn").attr("disabled", true);
                            if(failedCount>0){
                                var result = confirm("存在发送失败的邮件，如果删除记录，将无法重发，是否仍要删除?");
                                if(result){
                                    $.post("/delete", {"subject": $("#subject").text()}, function (data) {
                                        alert(data);
                                    });
                                }else{
                                    $("#deleteBtn").attr("disabled", false);
                                }
                            }else{
                                $.post("/delete", {"subject": $("#subject").text()}, function (data) {
                                    alert(data);
                                });
                            }
                        });
                    }
                }
            }
        };

        // 异常
        webSocket.onerror = function (event) {
            heartflag = false;
        };

        // 建立连接
        webSocket.onopen = function (event) {
            heartflag = true;
            heart();
            tryTime = 0;
            webSocket.send("first");
        };

        // 断线重连
        webSocket.onclose = function () {
            heartflag = false;
            // 重试10次，每次之间间隔10秒
            if (tryTime <= 10) {
                setTimeout(function () {
                    webSocket = null;
                    tryTime++;
                    initSocket();
                    alert("第" + tryTime + "次重连");
                }, 3 * 1000);
            } else {
                alert("重连失败.");
            }
        };
    }

    function heart() {
        if (heartflag){
            webSocket.send("&");
        }
    }