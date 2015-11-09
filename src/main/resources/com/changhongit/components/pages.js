function pageinputcheck(basePath, paramater, curPage, totalPage) {
	var tid = document.getElementById("Pages-btn-pageinput").value;
	if (tid == null || tid == "") {
		return;
	}
    if(!/^[0-9]*$/.test(tid)){
        alert("请输入数字!");
        return;
    }
	if (tid > totalPage)
		tid = totalPage;
	var url = basePath + '/' + tid + paramater;
	window.location.href = url;
}
