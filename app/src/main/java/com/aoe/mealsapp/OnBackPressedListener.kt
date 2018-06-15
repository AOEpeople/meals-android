package com.aoe.mealsapp

/**
 * To be implemented by fragments that want to be notified of parent activity's onBackPressed()
 */
interface OnBackPressedListener {

    /**
     * @return true if back press has been handled, false otherwise
     */
    fun onBackPressed(): Boolean
}
