import { RouterProvider } from "react-router";
import { router } from "./routes";
import { Toaster } from "../components/ui/sonner";
import { store } from "../lib/store";
import { Provider } from "react-redux";

export default function App() {
  return (
    <>
      <Provider store={store}>
        <RouterProvider router={router} />
        <Toaster />
      </Provider>
    </>
  );
}
