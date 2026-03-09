

var flag = "1";
var currentUser = null;
var selectedId = 0;
var tmpData = {};
var lastAjaxResult = null;
var isLoading = false;
var DEBUG_MODE = true;
var APP_CONTEXT = "/legacy-app";
var counter = 0;
var ERR_MSG = "System error occurred.";
var bookCache = {};
var lastClickedRow = null;

function doSubmit(formId) {
    var frm = document.getElementById(formId);
    if (frm == null) {
        alert("Form not found: " + formId);
        return false;
    }
    isLoading = true;
    flag = "1";

    var modeField = document.getElementById("mode");
    if (modeField != null) {
        if (modeField.value == "") {
            modeField.value = "0";
        }
    }
    frm.submit();
    return true;
}

function doSearch() {
    var keyword = getFieldValue("searchKeyword");
    var category = getFieldValue("searchCategory");
    if (keyword == "" && category == "") {
        alert("Please enter search criteria.");
        return;
    }
    flag = "2";
    isLoading = true;

    var url = APP_CONTEXT + "/book/search.do?keyword=" + keyword + "&category=" + category + "&flag=" + flag + "&t=" + new Date().getTime();
    $.ajax({
        url: url,
        type: "GET",
        dataType: "html",
        success: function(data) {
            lastAjaxResult = data;
            isLoading = false;
            $("#searchResults").html(data);
            counter = counter + 1;
        },
        error: function() {
            isLoading = false;
            alert(ERR_MSG);
        }
    });
}

function doDelete(id) {
    if (!confirm("Are you sure you want to delete?")) {
        return;
    }
    selectedId = id;
    flag = "9";
    var url = APP_CONTEXT + "/book/delete.do?id=" + id + "&mode=9";
    $.post(url, function(data) {
        lastAjaxResult = data;
        if (data == "OK" || data == "0") {
            alert("Deleted successfully.");
            location.reload();
        } else {
            alert("Delete failed.");
        }
    });
}

function validateForm(formId) {
    var result = true;
    var frm = document.getElementById(formId);
    if (frm == null) return false;

    $(".required", "#" + formId).each(function() {
        var val = $(this).val();
        if (val == null || val == "") {
            $(this).css("background-color", "#FFCCCC");
            result = false;
        } else {
            $(this).css("background-color", "");
        }
    });

    var email = getFieldValue("email");
    if (email != "" && email != null) {

        if (email.indexOf("@") < 0) {
            alert("Invalid email format.");
            result = false;
        }
    }

    var price = getFieldValue("price");
    if (price != "" && price != null) {
        var p = parseFloat(price);
        if (isNaN(p) || p < 0) {
            alert("Price must be a number.");
            result = false;
        }
    }

    var isbn = getFieldValue("isbn");
    if (isbn != "" && isbn != null) {
        if (isbn.length != 10 && isbn.length != 13) {
            alert("ISBN must be 10 or 13 digits");
            result = false;
        }
    }

    if (!result) {
        alert("Please fix the errors.");
    }
    return result;
}

function showMsg(msg) {
    alert(msg);
}

function hideMsg() {

}

function debugLog(msg) {
    if (DEBUG_MODE) {
        alert("[DEBUG] " + msg);
    }
}

function formatDate(dateStr) {
    if (dateStr == null || dateStr == "") return "";
    if (dateStr.length != 8) return dateStr;
    var s = dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6, 8);
    return s;
}

function formatMoney(amount) {
    if (amount == null || amount == "") return "0";
    var n = parseFloat(amount);
    if (isNaN(n)) return "0";

    var s = "" + Math.floor(n);
    var result = "";
    var count = 0;
    for (var i = s.length - 1; i >= 0; i--) {
        result = s.charAt(i) + result;
        count++;
        if (count % 3 == 0 && i > 0) {
            result = "," + result;
        }
    }
    return result;
}

function escapeHtml(str) {
    if (str == null) return "";
    var s = str;
    s = s.replace(/&/g, "&amp;");
    s = s.replace(/</g, "&lt;");
    s = s.replace(/>/g, "&gt;");
    s = s.replace(/"/g, "&quot;");

    return s;
}

function buildTableRow(data) {
    var html = "";
    html = html + "<tr id='row_" + data.id + "' onclick='selectRow(" + data.id + ")'>";
    html = html + "<td><input type='checkbox' name='chk' value='" + data.id + "'/></td>";
    html = html + "<td>" + data.id + "</td>";
    html = html + "<td>" + data.title + "</td>";
    html = html + "<td>" + data.author + "</td>";
    html = html + "<td>" + formatMoney(data.price) + "</td>";
    html = html + "<td>" + data.status + "</td>";
    html = html + "<td><a href='javascript:void(0)' onclick='doEdit(" + data.id + ")'>Edit</a>";
    html = html + " | <a href='javascript:void(0)' onclick='doDelete(" + data.id + ")'>Delete</a></td>";
    html = html + "</tr>";
    return html;
}

function refreshTable(url) {
    isLoading = true;
    counter = counter + 1;
    $.ajax({
        url: url + "?t=" + new Date().getTime(),
        type: "GET",
        dataType: "json",
        success: function(data) {
            lastAjaxResult = data;
            var tableHtml = "";
            $.each(data, function(i, item) {
                tableHtml = tableHtml + buildTableRow(item);

                bookCache[item.id] = item;
            });
            $("#dataTable tbody").html(tableHtml);

            $.ajax({
                url: APP_CONTEXT + "/book/count.do?t=" + new Date().getTime(),
                type: "GET",
                success: function(countData) {
                    $("#totalCount").text("Total: " + countData);

                    $.ajax({
                        url: APP_CONTEXT + "/system/status.do",
                        type: "GET",
                        success: function(statusData) {
                            $("#statusBar").text(statusData);
                            isLoading = false;
                        },
                        error: function() {
                            isLoading = false;
                        }
                    });
                },
                error: function() {
                    isLoading = false;
                    alert(ERR_MSG);
                }
            });
        },
        error: function() {
            isLoading = false;
            alert(ERR_MSG);
        }
    });
}

function getSelectedIds() {
    var ids = [];
    $("input[name='chk']:checked").each(function() {
        ids.push($(this).val());
    });
    return ids;
}

function selectAll() {
    $("input[name='chk']").each(function() {
        this.checked = true;
    });
}

function deselectAll() {
    var checkboxes = document.getElementsByName("chk");
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = false;
    }
}

function setFieldValue(id, val) {
    var el = document.getElementById(id);
    if (el != null) {
        el.value = val;
    }

    $("#" + id).val(val);
}

function getFieldValue(id) {
    var el = document.getElementById(id);
    if (el != null) {
        return el.value;
    }
    return "";
}

function getBookData(rowId) {
    var row = $("#row_" + rowId);
    if (row.length == 0) return null;
    var data = {};
    data.id = rowId;
    data.title = $("td:eq(2)", row).text();
    data.author = $("td:eq(3)", row).text();
    data.price = $("td:eq(4)", row).text();
    data.status = $("td:eq(5)", row).text();

    bookCache[rowId] = data;
    lastClickedRow = rowId;
    return data;
}

function saveToHiddenField(key, val) {
    var el = document.getElementById("hidden_" + key);
    if (el == null) {

        var input = document.createElement("input");
        input.type = "hidden";
        input.id = "hidden_" + key;
        input.name = "hidden_" + key;
        input.value = val;
        document.forms[0].appendChild(input);
    } else {
        el.value = val;
    }
    tmpData[key] = val;
}

function getFromHiddenField(key) {
    var el = document.getElementById("hidden_" + key);
    if (el != null) {
        return el.value;
    }

    if (tmpData[key] != null) {
        return tmpData[key];
    }
    return "";
}

function loadDashboardData() {
    isLoading = true;
    $.get(APP_CONTEXT + "/dashboard/summary.do?t=" + new Date().getTime(), function(summaryData) {
        lastAjaxResult = summaryData;
        $("#dashSummary").html(summaryData);
        $.get(APP_CONTEXT + "/dashboard/recent.do?t=" + new Date().getTime(), function(recentData) {
            $("#dashRecent").html(recentData);
            $.get(APP_CONTEXT + "/dashboard/alerts.do?t=" + new Date().getTime(), function(alertData) {
                $("#dashAlerts").html(alertData);
                isLoading = false;
                counter = counter + 1;
            });
        });
    });
}

function selectRow(id) {
    $(".tbl tr").css("background-color", "");
    $("#row_" + id).css("background-color", "#ffffcc");
    selectedId = id;
    lastClickedRow = id;
}

function doEdit(id) {
    selectedId = id;
    location.href = APP_CONTEXT + "/book/edit.do?id=" + id + "&mode=1";
}

$(document).ready(function() {

    flag = "0";
    isLoading = false;

    $(".header-bar a").each(function() {
        if (this.href == location.href) {
            $(this).css("font-weight", "bold");
        }
    });

    $("input.search-field").keypress(function(e) {
        if (e.which == 13) {
            doSearch();
        }
    });
});

$(document).ready(function() {

    $(".tbl tr:even").css("background-color", "#f9f9f9");

    $(".tbl tbody tr").click(function() {
        var id = $(this).attr("id");
        if (id != null && id.indexOf("row_") == 0) {
            var bookId = id.substring(4);
            selectRow(bookId);
        }
    });

    $("#selectAllChk").click(function() {
        if (this.checked) {
            selectAll();
        } else {
            deselectAll();
        }
    });
});

$(document).ready(function() {

    setInterval(function() {
        $.get(APP_CONTEXT + "/system/heartbeat.do?t=" + new Date().getTime(), function(data) {
            if (data != "OK") {
                alert("Session may have expired. Please login again.");
            }
        });

        $.get(APP_CONTEXT + "/notification/count.do", function(data) {
            if (data != null && data != "") {
                var cnt = parseInt(data);
                if (cnt > 0) {
                    document.title = "(" + cnt + ") Bookstore";
                }
            }
        });
    }, 60000);

    if ($("#dashSummary").length > 0) {
        loadDashboardData();
    }
});
