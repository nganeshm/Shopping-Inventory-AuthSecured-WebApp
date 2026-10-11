/**
 * User-facing copy for failures and confirmations (single source of truth).
 * Pages should prefer these keys over inline strings so messaging stays consistent.
 */
export const USER_MESSAGES = {
  generic: 'Something went wrong. Please try again.',
  network: 'We could not reach the server. Check your connection and try again.',

  auth: {
    loginFailed: 'Sign-in failed. Check your username and password.',
    registerFailed: 'Registration failed. Review your details and try again.',
    verifyEmailRequired: 'Verify your email before signing in.',
    emailVerified: 'Email verified. You can sign in now.',
    verifyFailed: 'That code is invalid or expired. Try again or resend.',
    otpResent: 'If the account can receive a code, a new OTP has been sent.',
    resendFailed: 'Could not resend the code. Try again in a moment.',
    forgotPasswordSent: 'If that email is registered, a code was sent.',
    forgotPasswordFailed: 'Could not send a reset code. Try again.',
    passwordUpdated: 'Password updated. Sign in with your new password.',
    passwordMismatch: 'The new passwords do not match.',
    resetFailed: 'Could not update the password. Check the code and try again.',
  },

  catalog: {
    loadProducts: 'Could not load products.',
    loadProduct: 'Product not found or could not be loaded.',
    loadCategories: 'Could not load categories.',
  },

  cart: {
    checkoutFailed: 'Could not place your order.',
    missingCustomerId:
      'We don\u2019t have your customer profile ID yet — set it on the Profile page, then check out.',
    addedToCart: 'Added to cart.',
  },

  orders: {
    loadList: 'Could not load orders.',
    loadDetail: 'Order not found or could not be loaded.',
  },

  profile: {
    loadCustomer: 'Could not load customer profile.',
    linkedCustomerId: 'Customer profile linked.',
    missingCustomerId: 'No customer id is linked to this account yet.',
    missingEmail: 'Sign in again so your session includes an email.',
  },

  admin: {
    createProduct: 'Could not create product.',
    createCategory: 'Could not create category.',
    loadLowStock: 'Could not load low-stock products.',
    restockFailed: 'Restock failed.',
    loadCategories: 'Could not load categories for this form.',
  },
}
