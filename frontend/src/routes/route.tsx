import { Link, Outlet } from "react-router-dom";
import { ErrorPage } from "../ErrorPage.tsx";
import Contact from "./Contact.tsx";
import { Demo2x2RedBlue } from "../demos/2x2/Demo2x2RedBlue.tsx";
import { Demo2x3x2RedAndBlue } from "../demos/2x3x2/Demo2x3x2RedAndBlue.tsx";
import React from "react";
import { Demo as Demo4Layers } from "../demos/2x3x3x3/Demo.tsx";
import { Demo as DemoDynamicShader } from "../demos/dynamic-shader/Demo.tsx";
import { Demo as MnistDemo } from "../demos/mnist/Demo.tsx";
import styled from "styled-components";

const path22 = "/ml/demos/22";
const path232 = `/ml/demos/232`;
const path4Layers = "/ml/demos/2333";
const pathDynamicShader = "/ml/demos/dynamic-demo";
export const routes = [
    {
        path: "/",
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: "/contacts/:contactId",
                element: <Contact />,
            },
            {
                path: path22,
                element: <Demo2x2RedBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path232,
                element: <Demo2x3x2RedAndBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path4Layers,
                element: <Demo4Layers />,
                errorElement: <ErrorPage />,
            },
            {
                path: pathDynamicShader,
                element: <DemoDynamicShader />,
                errorElement: <ErrorPage />,
            },
            { path: "MNIST", element: <MnistDemo />, errorElement: <ErrorPage /> },
        ],
    },
];

const Sidebar = styled.div`
    width: 22rem;
    background-color: #f7f7f7;
    border-right: solid 1px #e3e3e3;
    display: flex;
    flex-direction: column;
    min-width: fit-content;
`;
const Wrapper = styled.div`
    display: flex;
    grid-template-columns: 1fr 3fr;
`;
const Detail = styled.div`
    flex: 1;
    padding: 2rem;
    width: 100%;
`;
export default function Root() {
    return (
        <>
            <Wrapper>
                <Sidebar>
                    <h1>Demos side bar</h1>
                    <nav>
                        <ul>
                            <li>
                                <Link to="/">Home</Link>
                            </li>
                            <li>
                                <Link to={`/contacts/2`}>React Router Demo</Link>
                            </li>
                            <li>
                                <Link to={path22}>2x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path232}>2x3x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path4Layers}>2x3x3x3 Red/Green/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={pathDynamicShader}>Dynamic 2x3x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to="/MNIST">MNIST</Link>
                            </li>
                        </ul>
                    </nav>
                </Sidebar>
                <Detail>
                    <Outlet />
                </Detail>
            </Wrapper>
        </>
    );
}
