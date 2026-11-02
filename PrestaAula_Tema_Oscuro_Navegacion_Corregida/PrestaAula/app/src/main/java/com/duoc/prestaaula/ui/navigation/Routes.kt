package com.duoc.prestaaula.ui.navigation

object Routes {
    const val EQUIPMENT = "equipment"
    const val LOANS = "loans"
    const val REPORTS = "reports"
    const val PROFILE = "profile"

    const val EQUIPMENT_DETAIL = "equipment_detail/{id}"
    const val EQUIPMENT_FORM = "equipment_form/{id}"
    const val RESERVE = "reserve/{equipmentId}"
    const val LOAN_DETAIL = "loan/{id}"
    const val RETURN = "return/{loanId}"

    fun equipmentDetail(id: Long) = "equipment_detail/$id"
    fun equipmentForm(id: Long) = "equipment_form/$id"
    fun reserve(equipmentId: Long) = "reserve/$equipmentId"
    fun loanDetail(id: Long) = "loan/$id"
    fun returnLoan(loanId: Long) = "return/$loanId"
}
