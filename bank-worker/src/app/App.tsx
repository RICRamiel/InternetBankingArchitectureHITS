import { Provider } from "react-redux"
import { store } from "../lib/store"

import { RouterProvider } from "react-router"
import { router } from "./routes"
import { Toaster } from "../components/ui/sonner"

function App() {

  return (
    <>
      <Provider store={store}>
        <RouterProvider router={router} />
        <Toaster />
      </Provider>
    </>
  )
}

export default App
