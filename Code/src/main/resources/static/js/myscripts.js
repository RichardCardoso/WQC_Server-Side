function init() {
	var forms = document.getElementsByClassName('deleteForm');
	for (var index in forms) {
		var frm = forms[index];
		frm.onsubmit = function() {
			return confirm('Are you sure you want to delete this item?');
		}
	}
}